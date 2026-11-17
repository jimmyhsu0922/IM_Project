package com.example.improject.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.improject.R
import com.example.improject.domain.Role as UserRole

@Composable
fun BrandIcon(modifier: Modifier = Modifier) {
    Surface(modifier.size(48.dp), shape = RoundedCornerShape(15.dp), color = MaterialTheme.colorScheme.primary) {
        Icon(painterResource(R.drawable.ic_chat_brand), null, Modifier.padding(11.dp), tint = MaterialTheme.colorScheme.onPrimary)
    }
}

@Composable
fun AppHeader(title: String, subtitle: String, onLogout: (() -> Unit)? = null, onBack: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (onBack == null) BrandIcon() else {
            FilledIconButton(onClick = onBack, shape = RoundedCornerShape(15.dp), modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.ArrowBack, "返回")
            }
        }
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (onLogout != null) {
            OutlinedIconButton(onClick = onLogout, shape = RoundedCornerShape(14.dp), modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.ExitToApp, "登出", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun AppSegments(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(Modifier.padding(4.dp).selectableGroup(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            labels.forEachIndexed { index, label ->
                Surface(
                    modifier = Modifier.weight(if (label == "班級社團") 1.35f else 1f)
                        .clip(RoundedCornerShape(14.dp))
                        .selectable(selected == index, role = Role.Tab, onClick = { onSelect(index) }),
                    shape = RoundedCornerShape(14.dp),
                    color = if (selected == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Box(Modifier.heightIn(min = 48.dp).padding(horizontal = 4.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
                        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                            color = if (selected == index) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

/**
 * 頭像圓圈，依角色區分文字與顏色：
 * 學生顯示名字最後一個字（淺藍底）；老師顯示姓氏（奶油黃底，與老師的訊息泡泡同色）；
 * 管理員固定顯示「管」（深綠底）。
 */
@Composable
fun InitialAvatar(name: String, role: UserRole = UserRole.STUDENT) {
    val clean = name.trim()
    val (label, container, content) = when (role) {
        UserRole.TEACHER -> Triple(
            clean.take(1),
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        UserRole.ADMIN -> Triple(
            "管",
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary
        )
        UserRole.STUDENT -> Triple(
            clean.takeLast(1),
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
    Surface(shape = CircleShape, color = container, modifier = Modifier.size(36.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontWeight = FontWeight.Bold, color = content)
        }
    }
}

@Composable
fun DesignCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(modifier, shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), content = content)
}
