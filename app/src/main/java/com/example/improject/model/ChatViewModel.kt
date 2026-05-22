package com.example.improject.model

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    fun listenForMessages(groupId: String) {
        db.collection("messages")
            .whereEqualTo("groupId", groupId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    _messages.value = it.toObjects(ChatMessage::class.java)
                }
            }
    }

    fun sendMessage(text: String, senderName: String, senderId: String, groupId: String) {
        val msg = ChatMessage(
            senderName = senderName,
            text = text,
            senderId = senderId,
            groupId = groupId,
            timestamp = System.currentTimeMillis()
        )
        db.collection("messages").add(msg)
    }
}