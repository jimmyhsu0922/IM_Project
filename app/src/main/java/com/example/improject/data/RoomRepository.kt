package com.example.improject.data

import com.example.improject.domain.Announcement
import com.example.improject.domain.AppUser
import com.example.improject.domain.ChatMessage
import com.example.improject.domain.Role
import com.example.improject.domain.Room
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * rooms 集合與底下的 messages 子集合。
 * 寫入動作不等待伺服器回應：離線時訊息先出現在畫面，連線後自動送出；
 * 被安全規則拒絕時透過 onError 回報。
 */
class RoomRepository {
    private val rooms = FirebaseFirestore.getInstance().collection("rooms")

    fun observeRoom(roomId: String): Flow<Room?> = callbackFlow {
        val registration = rooms.document(roomId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(if (snapshot.exists()) snapshot.toRoom() else null)
            }
        }
        awaitClose { registration.remove() }
    }

    fun observeMessages(roomId: String): Flow<List<ChatMessage>> =
        rooms.document(roomId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limitToLast(MESSAGE_LIMIT)
            .asFlow { it.toChatMessage() }

    fun sendMessage(
        roomId: String,
        sender: AppUser,
        text: String,
        vocabId: String,
        category: Int,
        onError: (Throwable) -> Unit
    ) {
        val ref = rooms.document(roomId).collection("messages").document()
        val data = mapOf(
            "text" to text,
            "senderId" to sender.uid,
            "senderName" to sender.realName,
            "senderRole" to sender.role.key,
            "vocabId" to vocabId,
            "category" to category,
            "timestamp" to System.currentTimeMillis(),
            "edited" to false
        )
        ref.set(data).addOnFailureListener { onError(it) }
    }

    fun editMessage(roomId: String, messageId: String, newText: String, onError: (Throwable) -> Unit) {
        rooms.document(roomId).collection("messages").document(messageId)
            .update(mapOf("text" to newText, "edited" to true))
            .addOnFailureListener { onError(it) }
    }

    fun setAnnouncement(roomId: String, message: ChatMessage, onError: (Throwable) -> Unit) {
        val announcement = mapOf(
            "text" to message.text,
            "senderName" to message.senderName,
            "messageId" to message.id
        )
        rooms.document(roomId)
            .update(mapOf("announcement" to announcement))
            .addOnFailureListener { onError(it) }
    }

    fun clearAnnouncement(roomId: String, onError: (Throwable) -> Unit) {
        rooms.document(roomId)
            .update(mapOf("announcement" to FieldValue.delete()))
            .addOnFailureListener { onError(it) }
    }

    private companion object {
        const val MESSAGE_LIMIT = 300L
    }
}

private fun DocumentSnapshot.toRoom(): Room {
    val raw = get("announcement") as? Map<*, *>
    val announcement = raw?.let {
        Announcement(
            text = it["text"] as? String ?: "",
            senderName = it["senderName"] as? String ?: "",
            messageId = it["messageId"] as? String ?: ""
        )
    }
    return Room(
        id = id,
        name = getString("name").orEmpty(),
        type = getString("type").orEmpty(),
        announcement = announcement
    )
}

private fun DocumentSnapshot.toChatMessage(): ChatMessage = ChatMessage(
    id = id,
    text = getString("text").orEmpty(),
    senderId = getString("senderId").orEmpty(),
    senderName = getString("senderName").orEmpty(),
    senderRole = Role.fromKey(getString("senderRole")),
    vocabId = getString("vocabId").orEmpty(),
    category = (getLong("category") ?: 0L).toInt(),
    timestamp = getLong("timestamp") ?: 0L,
    edited = getBoolean("edited") ?: false
)
