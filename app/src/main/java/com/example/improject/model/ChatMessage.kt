package com.example.improject.model

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ChatMessage(
    val id: String = "",
    val senderName: String = "",
    val text: String = "",
    val senderId: String = "",
    val groupId: String = "",
    val timestamp: Long = 0L
)

@Composable
fun AnnouncementBar(announcement: ChatMessage?) {
    if (announcement != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFF3CD))
                .border(1.dp, Color(0xFFFFC107))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📌 公告：",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF856404)
            )
            Text(
                text = announcement.text,
                fontSize = 15.sp,
                color = Color(0xFF533F03),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "— ${announcement.senderName}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: ChatMessage,
    isMe: Boolean,
    enableLongClick: Boolean = false,
    onLongClick: (ChatMessage) -> Unit = {},
    onEditClick: (ChatMessage) -> Unit = {} // 💡 新增：編輯點擊回呼
) {
    val isTeacherMessage = message.senderName.contains("老師")
    var showMenu by remember { mutableStateOf(false) }

    val backgroundColor = when {
        isMe -> Color(0xFFDCF8C6)
        isTeacherMessage -> Color(0xFFFFF3CD)
        else -> Color(0xFFEEEEEE)
    }

    val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        contentAlignment = alignment
    ) {
        Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
            if (!isMe) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = message.senderName,
                        fontSize = 13.sp,
                        fontWeight = if (isTeacherMessage) FontWeight.Bold else FontWeight.Medium,
                        color = if (isTeacherMessage) Color(0xFF856404) else Color.DarkGray,
                        modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                    )
                    if (isTeacherMessage) {
                        Text(
                            text = " 📢 廣播",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }

            Surface(
                shape = if (isMe) {
                    RoundedCornerShape(topStart = 16.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                } else {
                    RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                },
                color = backgroundColor,
                modifier = Modifier
                    .combinedClickable(
                        onClick = { },
                        onLongClick = {
                            if (enableLongClick && isMe) {
                                showMenu = true
                            }
                        }
                    ),
                shadowElevation = if (isTeacherMessage) 3.dp else 1.dp
            ) {
                Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        text = message.text,
                        fontSize = if (isTeacherMessage) 20.sp else 18.sp,
                        fontWeight = if (isTeacherMessage) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isTeacherMessage) Color(0xFF533F03) else Color.Unspecified
                    )

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        // 💡 選項一：設為頂端公告
                        DropdownMenuItem(
                            text = { Text("📌 設為頂端公告") },
                            onClick = {
                                onLongClick(message)
                                showMenu = false
                            }
                        )
                        // 💡 選項二：編輯訊息 (只有老師端開啟 enableLongClick 且是自己發的才會進來)
                        DropdownMenuItem(
                            text = { Text("✏️ 編輯訊息") },
                            onClick = {
                                onEditClick(message)
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}