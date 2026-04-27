package com.example.improject.model

import androidx.compose.runtime.*
import com.example.improject.model.ChatMessage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

// 1. 定義資料結構
data class ChatMessage(
    val id: String,
    val senderName: String,
    val text: String,
    val isMe: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuDrivenChatScreen(
    isStudent: Boolean = true, // 預設為學生模式
    onBackClick: () -> Unit = {}
) {
    // 模擬對話紀錄狀態
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage("1", "王老師", "各位同學，今天的作業大家都完成了嗎？", false)
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("三年二班", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE3F2FD) // 柔和的粉藍色
                )
            )
        },
        bottomBar = {
            // 根據身分顯示不同的輸入區
            if (isStudent) {
                StudentMenuInputArea { selectedText ->
                    messages = messages + ChatMessage(
                        id = System.currentTimeMillis().toString(),
                        senderName = "我",
                        text = selectedText,
                        isMe = true
                    )
                }
            } else {
                TeacherKeyboardInputArea { typedText ->
                    messages = messages + ChatMessage(
                        id = System.currentTimeMillis().toString(),
                        senderName = "王老師 (我)",
                        text = typedText,
                        isMe = true
                    )
                }
            }
        }
    ) { paddingValues ->
        // 2. 聊天對話流區塊
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { msg ->
                MessageBubble(message = msg)
            }
        }
    }
}

// 3. 學生專用：兩層式分類選單 (取代鍵盤)
@Composable
fun StudentMenuInputArea(onMessageSend: (String) -> Unit) {
    // 預設詞彙庫
    val vocabularyMap = mapOf(
        "回報狀況" to listOf("我完成了！", "我需要幫忙", "我寫好了"),
        "情緒表達" to listOf("我很開心 😊", "我有點累 😕", "我聽不懂 ❓"),
        "打招呼" to listOf("老師好！", "謝謝老師", "收到！")
    )

    var selectedCategory by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(8.dp)
    ) {
        // 第一層：分類按鈕
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            vocabularyMap.keys.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        // 點擊切換分類
                        selectedCategory = if (selectedCategory == category) null else category
                    },
                    label = { Text(category, fontSize = 20.sp) }, // 字體加大，適合小學生
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        // 第二層：具體詞彙選單 (當有選擇分類時才顯示)
        selectedCategory?.let { category ->
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                vocabularyMap[category]?.forEach { phrase ->
                    Button(
                        onClick = {
                            onMessageSend(phrase)
                            selectedCategory = null // 送出後收合選單
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(phrase, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

// 4. 老師專用：傳統文字輸入框
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

// 5. 對話泡泡 UI
@Composable
fun MessageBubble(message: ChatMessage) {
    val backgroundColor = if (message.isMe) Color(0xFFDCF8C6) else Color.White
    val alignment = if (message.isMe) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(horizontalAlignment = if (message.isMe) Alignment.End else Alignment.Start) {
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
                    fontSize = 20.sp, // 字體加大，適合小學生閱讀
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}