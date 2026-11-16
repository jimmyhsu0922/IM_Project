package com.example.improject.ui.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.improject.domain.CATEGORY_COUNT
import com.example.improject.domain.ChatMessage
import com.example.improject.domain.GradeBand
import com.example.improject.domain.VocabEntry
import com.example.improject.ui.theme.CrayonColors
import com.example.improject.ui.theme.CrayonEdges
import com.example.improject.ui.theme.Ink

/**
 * 學生的「貼紙簿」：只能點選句庫裡的句子發言，每一句是一張粉彩色的貼紙。
 * 別人傳來的最新一句若在句庫裡，最上面會出現奶油黃的推薦回應。
 *
 * onSend(文字, 句子代碼, 分類)：送出推薦回應時，句子代碼是「被回應的那一句」的代碼，
 * 安全規則會檢查文字確實是該句子本身或它的推薦回應。
 */
@Composable
fun VocabularyKeyboard(
    band: GradeBand,
    entries: List<VocabEntry>,
    latestOtherMessage: ChatMessage?,
    onSend: (text: String, vocabId: String, category: Int) -> Unit
) {
    val trayShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

    if (entries.isEmpty()) {
        Surface(
            shape = trayShape,
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "句庫還是空的。請老師或管理員先匯入句子。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(20.dp)
            )
        }
        return
    }

    var selectedCategory by remember { mutableIntStateOf(1) }
    // 已經回應過的訊息代碼：回應後就收起推薦，避免干擾。
    var answeredMessageId by remember { mutableStateOf("") }

    val matched: VocabEntry? = remember(latestOtherMessage, entries) {
        val target = latestOtherMessage?.text?.trim().orEmpty()
        if (target.isBlank()) null else entries.firstOrNull { it.text.trim() == target }
    }
    val latestId = latestOtherMessage?.id.orEmpty()
    val suggestions: List<String> =
        if (matched != null && latestId.isNotBlank() && latestId != answeredMessageId) {
            matched.suggestions
        } else {
            emptyList()
        }
    val matchedId = matched?.id.orEmpty()
    val matchedCategory = matched?.category ?: 0

    val colorIndex = (selectedCategory - 1).coerceIn(0, CATEGORY_COUNT - 1)

    Surface(
        shape = trayShape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 12.dp)) {

            // 推薦回應：黃色，放在最上面，因為它是「現在最適合說的話」
            AnimatedVisibility(
                visible = suggestions.isNotEmpty(),
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Text(
                        text = "可以這樣回",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(suggestions) { suggestion: String ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        onSend(suggestion, matchedId, matchedCategory)
                                        answeredMessageId = latestId
                                    }
                            ) {
                                Text(
                                    text = suggestion,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 分類
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                items((1..CATEGORY_COUNT).toList()) { category: Int ->
                    val isSelected = selectedCategory == category
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) CrayonColors[category - 1] else Color.Transparent,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) CrayonEdges[category - 1] else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { selectedCategory = category }
                    ) {
                        Text(
                            text = band.categoryName(category),
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            color = if (isSelected) Ink else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            val filtered = remember(selectedCategory, entries) {
                entries.filter { it.category == selectedCategory }
            }

            // 貼紙
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                items(filtered, key = { it.id }) { vocab: VocabEntry ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CrayonColors[colorIndex],
                        border = BorderStroke(1.dp, CrayonEdges[colorIndex]),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                onSend(vocab.text, vocab.id, vocab.category)
                                answeredMessageId = latestId
                            }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = vocab.text,
                                fontSize = 15.sp,
                                lineHeight = 19.sp,
                                fontWeight = FontWeight.Normal,
                                color = Ink,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
