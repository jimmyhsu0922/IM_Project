package com.example.improject.ui.theme

import androidx.compose.ui.graphics.Color

// 校園小神通的色彩（柔和版）：整體降低對比，長時間看不刺眼。
// 文字用深灰綠而不是純黑，底色用帶一點綠的淺灰白，重點色都是低彩度的粉彩。

val Sage = Color(0xFF5C8A74)        // 主色：鼠尾草綠（按鈕、選取狀態）
val SageSoft = Color(0xFFDCE9E1)    // 淺綠：頂端列、登入頁上半、自己的訊息
val SageDeep = Color(0xFF2F5445)    // 淺綠上的文字
val Paper = Color(0xFFF5F7F4)       // 背景
val Ink = Color(0xFF33403A)         // 主要文字（深灰綠，不是黑）
val InkSoft = Color(0xFF6B7872)     // 次要文字
val Butter = Color(0xFFF7E9BC)      // 奶油黃：老師的訊息、公告、推薦回應
val ButterDeep = Color(0xFF7A6426)  // 奶油黃上的標示文字
val SoftRed = Color(0xFFB5544A)     // 錯誤訊息
val Hairline = Color(0xFFDCE4DE)    // 細框線

/** 七個句子分類的粉彩色，以及只深一點點的邊框色。順序對應分類 1～7。 */
val CrayonColors = listOf(
    Color(0xFFF9DDD4), // 問候與日常：桃
    Color(0xFFF8EBC2), // 回應與確認：杏黃
    Color(0xFFD6EAF3), // 讚美與肯定：天藍
    Color(0xFFDDEED0), // 感謝與道歉：草綠
    Color(0xFFE6DDF4), // 關心與鼓勵：紫藤
    Color(0xFFF6DCE6), // 合作與團隊：粉
    Color(0xFFD3EEE6)  // 求助與師生：薄荷
)
val CrayonEdges = listOf(
    Color(0xFFEFC4B6),
    Color(0xFFEBD899),
    Color(0xFFB7D7E6),
    Color(0xFFC3DDAF),
    Color(0xFFCFC0E8),
    Color(0xFFEBC0D0),
    Color(0xFFB3DDD0)
)
