package com.example.improject.model

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.improject.UserSession
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentChatScreen(
    userSession: UserSession,
    chatViewModel: ChatViewModel = viewModel(),
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("班級") }

    val currentGroupName = if (selectedTab == "班級") userSession.className else userSession.clubName
    val myId = userSession.myId

    LaunchedEffect(currentGroupName) {
        if (currentGroupName.isNotBlank()) {
            chatViewModel.listenForMessages(currentGroupName)
            chatViewModel.listenForAnnouncement(currentGroupName)
        }
    }

    val messages by chatViewModel.messages.collectAsState()
    val announcement by chatViewModel.currentAnnouncement.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$currentGroupName 聊天室 (${userSession.realName})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = if (selectedTab == "班級") 0 else 1) {
                Tab(
                    selected = selectedTab == "班級",
                    onClick = { selectedTab = "班級" },
                    text = { Text("我的班級") }
                )
                Tab(
                    selected = selectedTab == "社團",
                    onClick = { selectedTab = "社團" },
                    text = { Text("我的社團") }
                )
            }

            AnnouncementBar(announcement = announcement)

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 💡 修正：顯式加上 (msg: ChatMessage) 解決無法推導型別的 Bug
                items(messages) { msg: ChatMessage ->
                    MessageBubble(message = msg, isMe = msg.senderId == myId, enableLongClick = false)
                }
            }

            StudentMenuInput(
                onMessageSend = { text ->
                    chatViewModel.sendMessage(
                        text = text,
                        senderName = userSession.realName,
                        senderId = myId,
                        groupId = currentGroupName
                    )
                }
            )
        }
    }
}

// 💡 補上先前遺失的 StudentMenuInput 元件
@Composable
fun StudentMenuInput(onMessageSend: (String) -> Unit) {
    var inputText by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            placeholder = { Text("輸入訊息...") },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                if (inputText.isNotBlank()) {
                    onMessageSend(inputText)
                    inputText = ""
                }
            }
        ) {
            Text("發送")
        }
    }
}