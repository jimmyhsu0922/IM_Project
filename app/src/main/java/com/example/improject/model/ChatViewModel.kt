package com.example.improject.model

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private var messageListener: ListenerRegistration? = null
    private var announcementListener: ListenerRegistration? = null

    // 儲存當前群組的所有聊天訊息列表
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    // 儲存當前群組的頂端公告訊息
    private val _currentAnnouncement = MutableStateFlow<ChatMessage?>(null)
    val currentAnnouncement: StateFlow<ChatMessage?> = _currentAnnouncement

    /**
     * 連線並即時監聽特定群組的聊天訊息
     */
    fun listenForMessages(groupId: String) {
        // 群組切換時停止舊監聽，避免舊聊天室的資料覆蓋目前畫面。
        messageListener?.remove()
        messageListener = db.collection("messages")
            .whereEqualTo("groupId", groupId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    _messages.value = it.toObjects(ChatMessage::class.java)
                }
            }
    }

    /**
     * 連線並即時監聽該群組的頂端公告
     */
    fun listenForAnnouncement(groupId: String) {
        // 公告監聽也必須隨聊天室切換。
        announcementListener?.remove()
        announcementListener = db.collection("announcements")
            .document(groupId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    _currentAnnouncement.value = snapshot.toObject(ChatMessage::class.java)
                } else {
                    _currentAnnouncement.value = null
                }
            }
    }

    /**
     * 發送新訊息上雲端
     * 💡 修正點：先向 Firestore 要一個自動生成的 ID 存入 ChatMessage 中，
     * 這樣後續「編輯訊息」時，手機端才能知道要修改雲端的哪一份文件！
     */
    fun sendMessage(text: String, senderName: String, senderId: String, groupId: String) {
        // 1. 先向 Firestore 獲取一個自動生成的全新 document 參照
        val docRef = db.collection("messages").document()

        // 2. 將這個隨機生成的唯一識別碼（docRef.id）指定給 ChatMessage 的 id 欄位
        val msg = ChatMessage(
            id = docRef.id,
            senderName = senderName,
            text = text,
            senderId = senderId,
            groupId = groupId,
            timestamp = System.currentTimeMillis()
        )

        // 3. 用 set() 將整份帶有真正識別碼的資料寫入 Firestore
        docRef.set(msg)
    }

    /**
     * 將特定訊息設定為該群組的公告
     */
    fun setAnnouncement(message: ChatMessage) {
        if (message.groupId.isNotBlank()) {
            db.collection("announcements")
                .document(message.groupId)
                .set(message)
        }
    }

    /**
     * 編輯/更新現有的訊息內容
     * 💡 運作原理：依據傳入的 messageId 去雲端尋找對應的文件，並單獨更新 "text" 欄位。
     * 因為 listenForMessages 正在監聽，雲端一變動，所有人的手機畫面就會自動更新！
     */
    fun updateMessage(messageId: String, newText: String) {
        if (messageId.isNotBlank()) {
            db.collection("messages")
                .document(messageId)
                .update("text", newText)
        }
    }

    override fun onCleared() {
        messageListener?.remove()
        announcementListener?.remove()
    }
}
