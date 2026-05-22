package com.example.improject.model

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.improject.UserSession

// 1. 更新後的資料模型：符合 Firestore 自動轉換需求
data class ChatMessage(
    val id: String = "",
    val senderName: String = "",
    val text: String = "",
    val senderId: String = "", // 用來判斷是不是「我」發的
    val groupId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuDrivenChatScreen(
    isStudent: Boolean = true,
    userSession: UserSession,
    onBackClick: () -> Unit = {},
    // 直接注入同目錄下的 ChatViewModel
    chatViewModel: ChatViewModel = ChatViewModel()
) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    // 決定當前要顯示/監聽的群組
    val currentGroupName = if (isStudent) {
        if (selectedTabIndex == 0) userSession.className else userSession.clubName
    } else {
        userSession.teacherGroup
    }

    // 模擬一個當前使用者 ID (實務上應從 Auth 取得)
    val myId = if (isStudent) "std_${userSession.className}" else "tch_${userSession.teacherGroup}"

    // 【關鍵】當群組名稱改變時，告訴 ViewModel 切換監聽對象
    LaunchedEffect(currentGroupName) {
        chatViewModel.listenForMessages(currentGroupName)
    }

    // 觀察來自 ViewModel 的即時訊息流
    val messages by chatViewModel.messages.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(currentGroupName.ifEmpty { "未命名群組" }, fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFE3F2FD))
                )
                if (isStudent) {
                    TabRow(selectedTabIndex = selectedTabIndex) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = { Text("我的班級") }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = { Text("我的社團") }
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (isStudent) {
                StudentMenuInputArea { selectedText ->
                    chatViewModel.sendMessage(
                        text = selectedText,
                        senderName = "學生",
                        senderId = myId,
                        groupId = currentGroupName
                    )
                }
            } else {
                TeacherKeyboardInputArea { typedText ->
                    chatViewModel.sendMessage(
                        text = typedText,
                        senderName = "老師",
                        senderId = myId,
                        groupId = currentGroupName
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { msg ->
                // 透過 ID 比對判斷這則訊息是否由當前裝置發出
                MessageBubble(message = msg, isMe = msg.senderId == myId)
            }
        }
    }
}

// 學生專用：按鈕選單 (保持不變)
@Composable
fun StudentMenuInputArea(onMessageSend: (String) -> Unit) {
    val vocabularyMap = mapOf(
        "回報狀況" to listOf("我完成了！", "我需要幫忙", "我寫好了"),
        "情緒表達" to listOf("我很開心 😊", "我有點累 😕", "我聽不懂 ❓"),
        "打招呼" to listOf("老師好！", "謝謝老師", "收到！")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        vocabularyMap.forEach { (category, phrases) ->
            var expanded by remember { mutableStateOf(false) }
            Box {
                Button(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(category, fontSize = 18.sp)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    phrases.forEach { phrase ->
                        DropdownMenuItem(
                            text = { Text(phrase, fontSize = 18.sp) },
                            onClick = {
                                onMessageSend(phrase)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// 老師專用：鍵盤輸入 (保持不變)
@Composable
fun TeacherKeyboardInputArea(onMessageSend: (String) -> Unit) {
    var textState by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = textState,
            onValueChange = { textState = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("輸入老師指令或公告...") },
            shape = RoundedCornerShape(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = {
                if (textState.isNotBlank()) {
                    onMessageSend(textState)
                    textState = ""
                }
            },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Send, contentDescription = "發送", tint = Color.White)
        }
    }
}

// 氣泡對話框：新增 isMe 參數
@Composable
fun MessageBubble(message: ChatMessage, isMe: Boolean) {
    val backgroundColor = if (isMe) Color(0xFFDCF8C6) else Color.White
    val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
            Text(
                text = message.senderName,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = backgroundColor,
                shadowElevation = 1.dp,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = message.text,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}