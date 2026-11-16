package com.example.improject.ui.teacher

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.improject.domain.CATEGORY_COUNT
import com.example.improject.domain.CATEGORY_LABELS
import com.example.improject.domain.ChatMessage
import com.example.improject.domain.Role
import com.example.improject.ui.theme.appTopBarColors

/** 老師的統計畫面：這個聊天室裡學生用了哪些分類的句子、每位學生發言幾次。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherStatsScreen(
    roomName: String,
    messages: List<ChatMessage>,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val studentMessages = remember(messages) { messages.filter { it.senderRole == Role.STUDENT } }
    val byCategory = remember(studentMessages) {
        (1..CATEGORY_COUNT).map { category -> studentMessages.count { it.category == category } }
    }
    val byStudent = remember(studentMessages) {
        studentMessages
            .groupBy { it.senderId }
            .map { entry -> entry.value.last().senderName to entry.value.size }
            .sortedByDescending { it.second }
    }
    val categoryMax = byCategory.maxOrNull() ?: 0
    val studentMax = byStudent.maxOfOrNull { it.second } ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$roomName 互動統計") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = appTopBarColors()
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "統計範圍：最近 300 則訊息中，學生發出的 ${studentMessages.size} 則。",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("各類句子使用次數", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            }
            items((1..CATEGORY_COUNT).toList()) { category: Int ->
                StatBar(
                    label = CATEGORY_LABELS[category - 1],
                    count = byCategory[category - 1],
                    max = categoryMax
                )
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("每位學生發言次數", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                if (byStudent.isEmpty()) {
                    Text("還沒有學生發言。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(byStudent) { pair: Pair<String, Int> ->
                StatBar(label = pair.first, count = pair.second, max = studentMax)
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun StatBar(label: String, count: Int, max: Int) {
    val fraction = if (max > 0) count.toFloat() / max.toFloat() else 0f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, modifier = Modifier.width(88.dp), maxLines = 1)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(18.dp)
                .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(18.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
            )
        }
        Text(
            count.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .width(40.dp)
                .padding(start = 8.dp)
        )
    }
}
