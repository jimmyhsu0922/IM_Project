package com.example.improject.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.improject.data.AuthRepository
import com.example.improject.data.RoomRepository
import com.example.improject.data.VocabularyRepository
import com.example.improject.data.toFriendlyMessage
import com.example.improject.domain.AppUser
import com.example.improject.domain.ChatMessage
import com.example.improject.domain.GradeBand
import com.example.improject.domain.Room
import com.example.improject.domain.VocabEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** 聊天室畫面的狀態，學生端與老師端共用。 */
class ChatViewModel : ViewModel() {
    private val auth = AuthRepository()
    private val rooms = RoomRepository()
    private val vocabularyRepo = VocabularyRepository()

    private val _room = MutableStateFlow<Room?>(null)
    val room: StateFlow<Room?> = _room

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    /** 目前年段「已啟用」的句子。 */
    private val _vocabulary = MutableStateFlow<List<VocabEntry>>(emptyList())
    val vocabulary: StateFlow<List<VocabEntry>> = _vocabulary

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var currentRoomId: String = ""
    private var roomJob: Job? = null
    private var vocabularyJob: Job? = null

    /** 切換聊天室：先停掉舊的監聽，避免舊聊天室的資料蓋到新畫面。 */
    fun openRoom(roomId: String) {
        if (roomId == currentRoomId) return
        currentRoomId = roomId
        roomJob?.cancel()
        _room.value = null
        _messages.value = emptyList()
        if (roomId.isBlank()) return
        roomJob = viewModelScope.launch {
            launch {
                try {
                    rooms.observeRoom(roomId).collect { _room.value = it }
                } catch (e: Exception) {
                    report(e)
                }
            }
            launch {
                try {
                    rooms.observeMessages(roomId).collect { _messages.value = it }
                } catch (e: Exception) {
                    report(e)
                }
            }
        }
    }

    fun loadVocabulary(band: GradeBand) {
        vocabularyJob?.cancel()
        vocabularyJob = viewModelScope.launch {
            try {
                vocabularyRepo.observeBand(band).collect { list ->
                    _vocabulary.value = list.filter { it.enabled }
                }
            } catch (e: Exception) {
                report(e)
            }
        }
    }

    /** 老師端：載入全部年段已啟用的句子。 */
    fun loadAllVocabulary() {
        vocabularyJob?.cancel()
        vocabularyJob = viewModelScope.launch {
            try {
                vocabularyRepo.observeAll().collect { list ->
                    _vocabulary.value = list.filter { it.enabled }
                }
            } catch (e: Exception) {
                report(e)
            }
        }
    }

    fun send(sender: AppUser, text: String, vocabId: String = "", category: Int = 0) {
        val clean = text.trim()
        if (clean.isBlank() || currentRoomId.isBlank()) return
        rooms.sendMessage(currentRoomId, sender, clean, vocabId, category) { report(it) }
    }

    fun editMessage(message: ChatMessage, newText: String) {
        val clean = newText.trim()
        if (clean.isBlank() || currentRoomId.isBlank()) return
        rooms.editMessage(currentRoomId, message.id, clean) { report(it) }
    }

    fun setAnnouncement(message: ChatMessage) {
        if (currentRoomId.isBlank()) return
        rooms.setAnnouncement(currentRoomId, message) { report(it) }
    }

    fun clearAnnouncement() {
        if (currentRoomId.isBlank()) return
        rooms.clearAnnouncement(currentRoomId) { report(it) }
    }

    fun clearError() {
        _error.value = null
    }

    private fun report(e: Throwable) {
        if (e is kotlinx.coroutines.CancellationException) return
        // 已經登出：監聽被伺服器中斷是正常的，不需要提示。
        if (auth.currentUid == null) return
        _error.value = e.toFriendlyMessage()
    }
}
