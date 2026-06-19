package com.example.improject.model

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.improject.UserSession
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.improject.vocabulary.Vocabulary
import com.example.improject.vocabulary.LowerGradeRepo
import com.example.improject.vocabulary.MiddleGradeRepo
import com.example.improject.vocabulary.UpperGradeRepo
import com.example.improject.vocabulary.MiddleVocabulary

data class CategoryItem(val id: Int, val name: String, val color: Color)

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
                title = {
                    val cleanRealName = userSession.realName.replace(currentGroupName, "").trim()
                    Text("$currentGroupName 聊天室 ($cleanRealName)")
                },
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
                items(messages) { msg: ChatMessage ->
                    MessageBubble(message = msg, isMe = msg.senderId == myId, enableLongClick = false)
                }
            }

            // 🎯 將最新訊息列表串進去 Input 控制端
            StudentMenuInput(
                studentId = myId,
                messages = messages,
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

@Composable
fun StudentMenuInput(
    studentId: String,
    messages: List<ChatMessage>,
    onMessageSend: (String) -> Unit
) {
    val gradeChar = studentId.firstOrNull() ?: '1'

    val categories = listOf(
        CategoryItem(1, "主題一", Color(0xFFFFE4E1)),
        CategoryItem(2, "主題二", Color(0xFFFFFACD)),
        CategoryItem(3, "主題三", Color(0xFFE0FFFF)),
        CategoryItem(4, "主題四", Color(0xFFF0FFF0)),
        CategoryItem(5, "主題五", Color(0xFFE6E6FA))
    )

    val emojiRegex = Regex("[\\x{1F300}-\\x{1F64F}\\x{1F680}-\\x{1F6FF}\\x{2600}-\\x{26FF}\\x{2700}-\\x{27BF}]")

    val latestOtherMessageText = remember(messages) {
        val lastMsg = messages.lastOrNull()
        if (lastMsg != null && lastMsg.senderId != studentId) {
            lastMsg.text.replace(emojiRegex, "").trim()
        } else {
            ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        when (gradeChar) {
            '1', '2' -> {
                val lowerCategories = listOf(
                    CategoryItem(1, "社交日常", categories[0].color),
                    CategoryItem(2, "讚美肯定", categories[1].color),
                    CategoryItem(3, "感恩禮貌", categories[2].color),
                    CategoryItem(4, "鼓勵打氣", categories[3].color),
                    CategoryItem(5, "團隊合作", categories[4].color)
                )
                // 🎯 修正處：將低年級的 relatedSuggestions 完美對應過去
                val convertedLowerVocab = LowerGradeRepo.vocabularyList.map { vocab ->
                    MiddleVocabulary(
                        id = vocab.id,
                        text = vocab.text,
                        category = vocab.category,
                        relatedSuggestions = vocab.relatedSuggestions // 完美橋接推薦對答！
                    )
                }
                GenericVocabularyKeyboard(
                    categories = lowerCategories,
                    vocabularyList = convertedLowerVocab,
                    latestOtherMessageText = latestOtherMessageText,
                    onMessageSend = onMessageSend
                )
            }
            '3', '4' -> {
                val middleCategories = listOf(
                    CategoryItem(1, "社交互動", categories[0].color),
                    CategoryItem(2, "讚美肯定", categories[1].color),
                    CategoryItem(3, "感恩禮貌", categories[2].color),
                    CategoryItem(4, "鼓勵打氣", categories[3].color),
                    CategoryItem(5, "團隊合作", categories[4].color)
                )
                GenericVocabularyKeyboard(
                    categories = middleCategories,
                    vocabularyList = MiddleGradeRepo.vocabularyList,
                    latestOtherMessageText = latestOtherMessageText,
                    onMessageSend = onMessageSend
                )
            }
            '5', '6' -> {
                val upperCategories = listOf(
                    CategoryItem(1, "日常互動", categories[0].color),
                    CategoryItem(2, "讚美肯定", categories[1].color),
                    CategoryItem(3, "感恩回應", categories[2].color),
                    CategoryItem(4, "關懷打氣", categories[3].color),
                    CategoryItem(5, "團隊認同", categories[4].color)
                )
                GenericVocabularyKeyboard(
                    categories = upperCategories,
                    vocabularyList = UpperGradeRepo.vocabularyList,
                    latestOtherMessageText = latestOtherMessageText,
                    onMessageSend = onMessageSend
                )
            }
            else -> {
                val lowerCategories = listOf(
                    CategoryItem(1, "社交日常", categories[0].color),
                    CategoryItem(2, "讚美肯定", categories[1].color),
                    CategoryItem(3, "感恩禮貌", categories[2].color),
                    CategoryItem(4, "鼓勵打氣", categories[3].color),
                    CategoryItem(5, "團隊合作", categories[4].color)
                )
                val convertedLowerVocab = LowerGradeRepo.vocabularyList.map { vocab ->
                    MiddleVocabulary(
                        id = vocab.id,
                        text = vocab.text,
                        category = vocab.category,
                        relatedSuggestions = vocab.relatedSuggestions
                    )
                }
                GenericVocabularyKeyboard(
                    categories = lowerCategories,
                    vocabularyList = convertedLowerVocab,
                    latestOtherMessageText = latestOtherMessageText,
                    onMessageSend = onMessageSend
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericVocabularyKeyboard(
    categories: List<CategoryItem>,
    vocabularyList: List<MiddleVocabulary>,
    latestOtherMessageText: String, // 🎯 傳入他人最新訊息
    onMessageSend: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(1) }
    var activeSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }

    val emojiRegex = Regex("[\\x{1F300}-\\x{1F64F}\\x{1F680}-\\x{1F6FF}\\x{2600}-\\x{26FF}\\x{2700}-\\x{27BF}]")

    // 🎯 關鍵連動機制：當別人的新訊息改變時，去對應資料庫找到他的推薦回應
    LaunchedEffect(latestOtherMessageText, vocabularyList) {
        if (latestOtherMessageText.isNotBlank()) {
            val cleanTarget = latestOtherMessageText.replace(emojiRegex, "").trim()
            // 在當前年級庫尋找是否有 match 的文字
            val matchedVocab = vocabularyList.find {
                it.text.replace(emojiRegex, "").trim() == cleanTarget
            }
            // 如果找到匹配，就把別人的推薦答案提取出來
            activeSuggestions = matchedVocab?.relatedSuggestions ?: emptyList()
        } else {
            // 如果最新一筆訊息是自己傳的，或目前空蕩蕩，就不顯示任何推薦
            activeSuggestions = emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            items(categories) { category: CategoryItem ->
                val isSelected = selectedCategory == category.id
                val cleanCategoryName = category.name.replace(emojiRegex, "").trim()
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) category.color else Color.LightGray.copy(alpha = 0.2f),
                    modifier = Modifier.clickable { selectedCategory = category.id }
                ) {
                    Text(
                        text = cleanCategoryName,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Text(
            text = "請點選你想說的話：",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        val filteredVocabulary = remember(selectedCategory, vocabularyList) {
            vocabularyList.filter { it.category == selectedCategory }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            items(filteredVocabulary) { vocab: MiddleVocabulary ->
                val cleanMainText = vocab.text.replace(emojiRegex, "").trim()

                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = categories.find { it.id == selectedCategory }?.color?.copy(alpha = 0.5f) ?: Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable {
                            onMessageSend(cleanMainText)
                            // 🎯 自己點擊主面板發送後，清空推薦，避免干擾
                            activeSuggestions = emptyList()
                        }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = cleanMainText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = activeSuggestions.isNotEmpty(),
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💡 回應推薦：",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(activeSuggestions) { suggestion: String ->
                            val cleanSuggestion = suggestion.replace(emojiRegex, "").trim()

                            SuggestionChip(
                                onClick = {
                                    // 🎯 點擊推薦直接送出回應，隨後隱藏
                                    onMessageSend(cleanSuggestion)
                                    activeSuggestions = emptyList()
                                },
                                label = { Text(cleanSuggestion, fontSize = 13.sp) },
                                shape = RoundedCornerShape(8.dp),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}