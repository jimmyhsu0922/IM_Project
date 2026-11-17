package com.example.improject.ui.teacher

import com.example.improject.ui.components.*
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
            AppHeader("互動統計", roomName, onBack = onBack)
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
                Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(18.dp)).padding(16.dp)) {
                    Text("最近 300 則聊天室訊息", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Text("其中有 ${studentMessages.size} 則學生發言", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("各類句子使用次數", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                DesignCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        (1..CATEGORY_COUNT).forEach { category ->
                            StatBar(CATEGORY_LABELS[category - 1], byCategory[category - 1], categoryMax)
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("每位學生發言次數", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                if (byStudent.isEmpty()) {
                    Text("還沒有學生發言。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                DesignCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        byStudent.forEach { pair -> StatBar(pair.first, pair.second, studentMax) }
                    }
                }
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
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, modifier = Modifier.width(98.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(5.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(10.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(5.dp))
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
