package com.example.improject.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import com.example.improject.ui.components.*
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.improject.ui.theme.appTopBarColors
import com.example.improject.domain.AppUser
import com.example.improject.domain.ChatMessage
import com.example.improject.ui.chat.ChatViewModel
import com.example.improject.ui.components.AnnouncementBar
import com.example.improject.ui.components.MessageBubble

/** 學生聊天室：班級與社團兩個分頁，只能用詞彙鍵盤發言。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentChatScreen(
    user: AppUser,
    onLogout: () -> Unit,
    chatViewModel: ChatViewModel = viewModel(key = "chat-${user.uid}")
) {
    val hasClub = user.clubId.isNotBlank()
    var selectedTab by remember { mutableIntStateOf(0) }
    val roomId = if (selectedTab == 1 && hasClub) user.clubId else user.classId

    LaunchedEffect(roomId) { chatViewModel.openRoom(roomId) }
    LaunchedEffect(user.gradeBand) { chatViewModel.loadVocabulary(user.gradeBand) }

    val room by chatViewModel.room.collectAsState()
    val messages by chatViewModel.messages.collectAsState()
    val vocabulary by chatViewModel.vocabulary.collectAsState()
    val error by chatViewModel.error.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    LaunchedEffect(error) {
        val text = error
        if (text != null) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            chatViewModel.clearError()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val lastMessage = messages.lastOrNull()
    val latestOtherMessage = if (lastMessage != null && lastMessage.senderId != user.uid) lastMessage else null

    Scaffold(
        topBar = {
            AppHeader(room?.name ?: "聊天室", "校園小神通", onLogout = onLogout)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (hasClub) {
            AppSegments(listOf("我的班級", "我的社團"), selectedTab) { selectedTab = it }
            }

            if (roomId.isBlank()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("還沒有分配班級，請聯絡老師。")
                }
                return@Column
            }

            AnnouncementBar(announcement = room?.announcement)

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { message: ChatMessage ->
                    MessageBubble(message = message, isMe = message.senderId == user.uid)
                }
            }

            VocabularyKeyboard(
                band = user.gradeBand,
                entries = vocabulary,
                latestOtherMessage = latestOtherMessage,
                onSend = { text, vocabId, category ->
                    chatViewModel.send(user, text, vocabId, category)
                }
            )
        }
    }
}
