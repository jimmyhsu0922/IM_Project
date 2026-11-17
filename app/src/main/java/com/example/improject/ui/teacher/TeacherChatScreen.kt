package com.example.improject.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import com.example.improject.ui.components.*
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.improject.ui.theme.appTopBarColors
import com.example.improject.domain.AppUser
import com.example.improject.domain.ChatMessage
import com.example.improject.domain.Role
import com.example.improject.ui.chat.ChatViewModel
import com.example.improject.ui.components.AnnouncementBar
import com.example.improject.ui.components.MessageBubble

/** 老師聊天室：自由輸入廣播、設定公告、編輯自己的訊息、查看互動統計。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherChatScreen(
    user: AppUser,
    onLogout: () -> Unit,
    chatViewModel: ChatViewModel = viewModel(key = "chat-${user.uid}")
) {
    val hasClass = user.classId.isNotBlank()
    val hasClub = user.clubId.isNotBlank()
    val showTabs = hasClass && hasClub
    var selectedTab by remember { mutableIntStateOf(0) }
    val roomId = when {
        showTabs -> if (selectedTab == 1) user.clubId else user.classId
        hasClass -> user.classId
        else -> user.clubId
    }

    LaunchedEffect(roomId) { chatViewModel.openRoom(roomId) }
    LaunchedEffect(Unit) { chatViewModel.loadAllVocabulary() }

    val room by chatViewModel.room.collectAsState()
    val messages by chatViewModel.messages.collectAsState()
    val error by chatViewModel.error.collectAsState()
    val vocabulary by chatViewModel.vocabulary.collectAsState()
    // 已經回應過的學生訊息：回應後收起推薦。
    var answeredMessageId by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current

    var showStats by remember { mutableStateOf(false) }
    var editingMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var editText by remember { mutableStateOf("") }

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

    if (showStats) {
        TeacherStatsScreen(
            roomName = room?.name ?: "聊天室",
            messages = messages,
            onBack = { showStats = false }
        )
        return
    }

    Scaffold(
        topBar = {
            AppHeader(room?.name ?: "聊天室", user.realName + " 老師", onLogout = onLogout)
        },
        // 系統列與鍵盤的空間由下面的 Column 自己處理，Scaffold 不再另外加。
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                // 底部只留「導覽列」和「鍵盤」兩者中較高的那個，鍵盤收起時也不會多出空白。
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
        ) {
            if (showTabs) {
            AppSegments(listOf("我的班級", "我的社團"), selectedTab) { selectedTab = it }
            }

            if (roomId.isBlank()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("還沒有指派班級或社團，請聯絡管理員。")
                }
                return@Column
            }

            AnnouncementBar(
                announcement = room?.announcement,
                onClear = { chatViewModel.clearAnnouncement() }
            )

            DesignCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)) {
                Row(Modifier.clickable { showStats = true }.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("▥", color = MaterialTheme.colorScheme.primary, fontSize = 22.sp)
                    Text("互動統計", Modifier.weight(1f).padding(start = 12.dp),
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("›", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { message: ChatMessage ->
                    MessageBubble(
                        message = message,
                        isMe = message.senderId == user.uid,
                        canManage = true,
                        onPin = { chatViewModel.setAnnouncement(it) },
                        onEdit = {
                            editingMessage = it
                            editText = it.text
                        }
                    )
                }
            }

            // 學生最新一句若設定了「老師回應」，在輸入框上方提供一鍵回覆。
            val lastMessage = messages.lastOrNull()
            val teacherReplies: List<String> =
                if (lastMessage != null &&
                    lastMessage.senderRole == Role.STUDENT &&
                    lastMessage.id != answeredMessageId
                ) {
                    vocabulary.firstOrNull { it.id == lastMessage.vocabId && it.text == lastMessage.text }
                        ?.teacherSuggestions ?: emptyList()
                } else {
                    emptyList()
                }
            if (teacherReplies.isNotEmpty() && lastMessage != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, top = 4.dp)
                ) {
                    Text(
                        "可以這樣回",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    teacherReplies.forEach { reply ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    chatViewModel.send(user, reply)
                                    answeredMessageId = lastMessage.id
                                }
                        ) {
                            Text(
                                reply,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Surface(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest) {
                Column(Modifier.padding(12.dp)) {
                    Text("長按自己的訊息可編輯或設為公告", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TeacherKeyboardInput(onMessageSend = { text -> chatViewModel.send(user, text) })
                }
            }
        }
    }

    val target = editingMessage
    if (target != null) {
        AlertDialog(
            onDismissRequest = { editingMessage = null },
            title = { Text("編輯訊息") },
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
                        chatViewModel.editMessage(target, editText)
                        editingMessage = null
                    }
                ) {
                    Text("儲存")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingMessage = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun TeacherKeyboardInput(onMessageSend: (String) -> Unit) {
    var inputText by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            placeholder = { Text("寫給全班的話") },
            maxLines = 3,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.heightIn(min = 54.dp),
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
