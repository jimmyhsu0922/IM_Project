package com.example.improject.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Sage,
    onPrimary = Color.White,
    primaryContainer = SageSoft,
    onPrimaryContainer = SageDeep,
    secondary = ButterDeep,
    onSecondary = Color.White,
    secondaryContainer = Butter,
    onSecondaryContainer = Ink,
    tertiary = Color(0xFF5E8296),
    onTertiary = Color.White,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE6ECE7),
    onSurfaceVariant = InkSoft,
    outline = Color(0xFFA9B6AF),
    outlineVariant = Hairline,
    error = SoftRed,
    onError = Color.White,
    surfaceContainerLowest = Color(0xFFFCFDFB),
    surfaceContainerLow = Color(0xFFF8FAF7),
    surfaceContainer = Color(0xFFEFF3EF),
    surfaceContainerHigh = Color(0xFFEAEFEA),
    surfaceContainerHighest = Color(0xFFE4EAE5)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9CC4B0),
    onPrimary = Color(0xFF1C3329),
    primaryContainer = Color(0xFF2F4439),
    onPrimaryContainer = Color(0xFFD5E5DC),
    secondary = Color(0xFFDCCB95),
    onSecondary = Color(0xFF3A2F0E),
    secondaryContainer = Color(0xFF4A4124),
    onSecondaryContainer = Color(0xFFF0E6C4),
    tertiary = Color(0xFFA9C7D6),
    onTertiary = Color(0xFF1B3340),
    background = Color(0xFF1B211E),
    onBackground = Color(0xFFD9E0DB),
    surface = Color(0xFF1B211E),
    onSurface = Color(0xFFD9E0DB),
    surfaceVariant = Color(0xFF323B36),
    onSurfaceVariant = Color(0xFFA9B5AE),
    outline = Color(0xFF74817A),
    outlineVariant = Color(0xFF39433D),
    error = Color(0xFFE5A097),
    onError = Color(0xFF4A1711),
    surfaceContainerLowest = Color(0xFF161B18),
    surfaceContainerLow = Color(0xFF202623),
    surfaceContainer = Color(0xFF252C28),
    surfaceContainerHigh = Color(0xFF2C3430),
    surfaceContainerHighest = Color(0xFF343D38)
)

// 圓角依層級區分：輸入框小、卡片中、貼紙托盤與對話框大。
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun IMProjectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // 不使用手機的動態配色，讓每支手機上看到的都是同一套配色。
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}

/** 所有畫面共用的頂端列配色：淺綠底配深綠字，深色模式會自動換成對應的暗色。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun appTopBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.primaryContainer,
    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
)
