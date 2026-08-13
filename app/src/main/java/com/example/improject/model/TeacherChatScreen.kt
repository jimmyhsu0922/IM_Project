package com.example.improject.model

import androidx.compose.foundation.background
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
fun TeacherChatScreen(
    userSession: UserSession,
    chatViewModel: ChatViewModel = viewModel(),
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("班級") }
    val classGroupName = userSession.className.ifBlank { userSession.teacherGroup }
    // 既有測試帳號尚未更新 Firestore 時，T201～T205 仍可使用預設社團設定。
    val clubGroupName = userSession.clubName.ifBlank { TeacherAssignments.clubFor(userSession.myId) }
    val hasClub = clubGroupName.isNotBlank()
    val currentGroupName = if (selectedTab == "社團" && hasClub) {
        clubGroupName
    } else {
        classGroupName
    }
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

    // 💡 用於控制「編輯訊息對話框」的狀態
    var showEditDialog by remember { mutableStateOf(false) }
    var editingMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var editText by remember { mutableStateOf("") }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$currentGroupName 聊天室 (老師介面)") },
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
            if (hasClub) {
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
            }

            Box(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.errorContainer).padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("💡 提示：長按您發布的訊息可設定為固定公告或進行編輯", color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 14.sp)
            }

            AnnouncementBar(announcement = announcement)

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 💡 修正：顯式加上 (msg: ChatMessage) 解決無法推導型別的 Bug
                items(messages) { msg: ChatMessage ->
                    MessageBubble(
                        message = msg,
                        isMe = msg.senderId == myId,
                        enableLongClick = true,
                        onLongClick = { selectedMsg ->
                            chatViewModel.setAnnouncement(selectedMsg)
                        },
                        onEditClick = { selectedMsg ->
                            // 💡 點擊編輯時，記錄當前訊息，打開 Dialog
                            editingMessage = selectedMsg
                            editText = selectedMsg.text
                            showEditDialog = true
                        }
                    )
                }
            }

            TeacherKeyboardInput(
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

    // 💡 老師專屬的編輯訊息對話框 (AlertDialog)
    if (showEditDialog && editingMessage != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("✏️ 編輯訊息內容") },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        chatViewModel.updateMessage(editingMessage!!.id, editText)
                        showEditDialog = false
                        editingMessage = null
                    }
                ) {
                    Text("確定修改")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

// 💡 補上先前遺失的 TeacherKeyboardInput 元件
@Composable
fun TeacherKeyboardInput(onMessageSend: (String) -> Unit) {
    var inputText by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            placeholder = { Text("以老師身份廣播訊息...") },
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
            Text("廣播")
        }
    }
}
