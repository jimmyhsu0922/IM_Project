package com.example.improject.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.improject.domain.Announcement
import com.example.improject.domain.ChatMessage
import com.example.improject.domain.Role

/** 小標籤：用在「公告」與「老師」。只用字重和顏色區分，不加深色底，避免太搶眼。 */
@Composable
private fun SoftTag(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

/** 聊天室頂端的公告列。onClear 不是 null 時（老師端）顯示「取消公告」。 */
@Composable
fun AnnouncementBar(announcement: Announcement?, onClear: (() -> Unit)? = null) {
    if (announcement == null) return
    val content = MaterialTheme.colorScheme.onSecondaryContainer
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp))
            .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "公告",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = announcement.text,
                fontSize = 16.sp,
                color = content,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = announcement.senderName,
                fontSize = 12.sp,
                color = content.copy(alpha = 0.6f)
            )
        }
        if (onClear != null) {
            TextButton(onClick = onClear) { Text("取消公告") }
        } else {
            Spacer(modifier = Modifier.width(12.dp))
        }
    }
}

/**
 * 訊息泡泡。
 * 自己的訊息：淺綠；同學的訊息：白底細框；老師的訊息：奶油黃並加上「老師」標籤。
 * 三種泡泡都是淺底深字，彼此靠色相區分，不靠明暗對比。
 * 老師的訊息用發送者的角色判斷（不是看姓名）。
 * canManage 為 true 時（老師看自己的訊息），長按可設為公告或編輯。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: ChatMessage,
    isMe: Boolean,
    canManage: Boolean = false,
    onPin: (ChatMessage) -> Unit = {},
    onEdit: (ChatMessage) -> Unit = {}
) {
    val isTeacherMessage = message.senderRole == Role.TEACHER
    var showMenu by remember { mutableStateOf(false) }

    val bubbleColor: Color
    val textColor: Color
    val border: BorderStroke?
    when {
        isMe -> {
            bubbleColor = MaterialTheme.colorScheme.primaryContainer
            textColor = MaterialTheme.colorScheme.onSurface
            border = null
        }
        isTeacherMessage -> {
            bubbleColor = MaterialTheme.colorScheme.secondaryContainer
            textColor = MaterialTheme.colorScheme.onSecondaryContainer
            border = null
        }
        else -> {
            bubbleColor = MaterialTheme.colorScheme.surfaceContainerLowest
            textColor = MaterialTheme.colorScheme.onSurface
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.Top) {
            if (!isMe) {
                InitialAvatar(message.senderName, message.senderRole)
                Spacer(Modifier.width(8.dp))
            }
            Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
            if (!isMe) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 6.dp, bottom = 3.dp)
                ) {
                    Text(
                        text = message.senderName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isTeacherMessage) {
                        Spacer(modifier = Modifier.width(6.dp))
                        SoftTag("老師")
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = bubbleColor,
                border = border,
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .combinedClickable(
                        onClick = { },
                        onLongClick = { if (canManage && isMe) showMenu = true }
                    )
            ) {
                Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Column {
                        Text(
                            text = message.text,
                            fontSize = if (isTeacherMessage) 18.sp else 17.sp,
                            lineHeight = 25.sp,
                            fontWeight = FontWeight.Normal,
                            color = textColor
                        )
                        if (message.edited) {
                            Text("已編輯", fontSize = 11.sp, color = textColor.copy(alpha = 0.6f))
                        }
                    }

                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("設為公告") },
                            onClick = {
                                onPin(message)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("編輯訊息") },
                            onClick = {
                                onEdit(message)
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
        }
    }
}
