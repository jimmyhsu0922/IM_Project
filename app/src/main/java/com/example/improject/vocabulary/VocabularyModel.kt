package com.example.improject.vocabulary

/**
 * 詞彙資料結構
 * @param text 詞彙按鈕顯示的文字 (例如: "你好呀")
 * @param category 屬於哪一個部分 (1~5)
 * @param recommendations 當「對方發送此字串」時，系統要「自動推薦給自己」的回應清單
 */

// 調整欄位順序，完美相容原本的填法，把新增的 id 與推薦往後擺！
data class Vocabulary(
    val text: String,                                  // 1. 原本的第一位
    val category: Int,                                 // 2. 原本的第二位
    val id: Int = 0,                                   // 3. 新增的 id（給預設值 0 避免出錯）
    val relatedSuggestions: List<String> = emptyList() // 4. 新增的回應推薦
)