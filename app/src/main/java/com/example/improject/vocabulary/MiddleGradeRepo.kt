package com.example.improject.vocabulary

// 中年級單一詞彙的資料結構
data class MiddleVocabulary(
    val id: Int,
    val text: String,
    val category: Int, // 1: 社交日常, 2: 讚美肯定, 3: 感恩禮貌, 4: 鼓勵打氣, 5: 團隊合作
    val relatedSuggestions: List<String> = emptyList() // 🎯 你的神設計：自動推薦的關聯回應
)

object MiddleGradeRepo {
    val vocabularyList = listOf(
        // === 1. 社交問候與日常互動 ===
        MiddleVocabulary(1, "今天過得好嗎", 1, listOf("很好啊", "有點累")),
        MiddleVocabulary(2, "很好啊", 1),
        MiddleVocabulary(3, "等一下要做什麼", 1),
        MiddleVocabulary(4, "作業寫完了嗎", 1, listOf("寫完了", "還沒寫完")),
        MiddleVocabulary(5, "寫完了", 1),
        MiddleVocabulary(6, "還沒寫完", 1),
        MiddleVocabulary(7, "週末要去哪裡玩", 1),
        MiddleVocabulary(8, "很高興認識你", 1),
        MiddleVocabulary(9, "好久不見耶", 1),
        MiddleVocabulary(10, "祝你有美好的一天", 1),

        // === 2. 讚美與正面肯定 ===
        MiddleVocabulary(11, "你今天很帥喔", 2, listOf("謝謝", "你也是")),
        MiddleVocabulary(12, "你今天很漂亮", 2, listOf("謝謝", "你也是")),
        MiddleVocabulary(13, "你字寫得真好看", 2),
        MiddleVocabulary(14, "這張圖畫得太棒了", 2),
        MiddleVocabulary(15, "這個點子真讚", 2),
        MiddleVocabulary(16, "你進步好多喔", 2),
        MiddleVocabulary(17, "你也是", 2),
        MiddleVocabulary(18, "很有創意耶", 2),
        MiddleVocabulary(19, "真是個好幫手", 2),
        MiddleVocabulary(20, "實力很強喔", 2),

        // === 3. 感恩與禮貌回應 ===
        MiddleVocabulary(21, "真的太謝謝你了", 3),
        MiddleVocabulary(22, "辛苦你囉", 3),
        MiddleVocabulary(23, "沒關係，別放在心上", 3),
        MiddleVocabulary(24, "這線是我應該做的", 3),
        MiddleVocabulary(25, "多虧有你幫忙", 3),
        MiddleVocabulary(26, "收到，謝謝你", 3),
        MiddleVocabulary(27, "抱歉讓你久等了", 3),
        MiddleVocabulary(28, "很榮幸能幫上忙", 3),
        MiddleVocabulary(29, "隨時樂意幫忙", 3),
        MiddleVocabulary(30, "謝謝你的分享", 3),

        // === 4. 鼓勵、打氣與關懷 ===
        MiddleVocabulary(31, "相信自己，你可以的", 4),
        MiddleVocabulary(32, "沒事沒事，下次再來", 4),
        MiddleVocabulary(33, "你已經很努力了", 4),
        MiddleVocabulary(34, "撐住，要堅持到底", 4),
        MiddleVocabulary(35, "保持微笑喔", 4),
        MiddleVocabulary(36, "有需要可以找我", 4),
        MiddleVocabulary(37, "別給自己太大壓力", 4),
        MiddleVocabulary(38, "失敗是成功的媽媽", 4),
        MiddleVocabulary(39, "抬起頭來，加油", 4),
        MiddleVocabulary(40, "我會一直支持你", 4),

        // === 5. 合作與團隊認同 ===
        MiddleVocabulary(41, "團結力量大", 5),
        MiddleVocabulary(42, "我們是一隊的", 5),
        MiddleVocabulary(43, "大家辛苦了", 5),
        MiddleVocabulary(44, "合作愉快", 5),
        MiddleVocabulary(45, "有你們這個隊友真好", 5),
        MiddleVocabulary(46, "我們一起討論看看", 5),
        MiddleVocabulary(47, "尊重你的選擇", 5),
        MiddleVocabulary(48, "聽聽看大家的意見", 5),
        MiddleVocabulary(49, "一起創造奇蹟吧", 5),
        MiddleVocabulary(50, "齊心協力拿第一", 5)
    )
}