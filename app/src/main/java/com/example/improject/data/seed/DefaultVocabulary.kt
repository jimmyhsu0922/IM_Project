package com.example.improject.data.seed

import com.example.improject.domain.GradeBand
import com.example.improject.domain.VocabEntry

/**
 * 預設句庫（第二版）：三個年段各 50 句，分成七類。
 * 只在管理員按「匯入預設句庫」時寫進資料庫，之後 App 一律讀資料庫裡的句子。
 *
 * replies：同學收到這句時，推薦給同學的回應。
 * teacherReplies：老師收到這句時，推薦給老師的回應。
 * caution：這句容易被拿來反諷，後台會標示提醒。
 * 分類編號對應 domain/Models.kt 的 CATEGORY_LABELS。
 */
private data class Seed(
    val text: String,
    val category: Int,
    val replies: List<String> = emptyList(),
    val teacherReplies: List<String> = emptyList(),
    val caution: Boolean = false
)

// 低年級（一、二年級）：短句直覺、字數 6 字以內
private val LOWER = listOf(
    Seed("早安", 1, replies = listOf("早安")),
    Seed("你好呀", 1, replies = listOf("你好呀")),
    Seed("拜拜", 1, replies = listOf("拜拜", "明天見")),
    Seed("明天見", 1, replies = listOf("明天見", "拜拜")),
    Seed("放學了", 1, replies = listOf("明天見", "拜拜")),
    Seed("你在做什麼", 1, replies = listOf("我在寫功課", "等我一下")),
    Seed("我在寫功課", 1),
    Seed("生日快樂", 1, replies = listOf("謝謝")),
    Seed("好啊", 2),
    Seed("等我一下", 2),
    Seed("對啊", 2),
    Seed("謝謝，我還好", 2),
    Seed("好，謝謝", 2),
    Seed("下次再玩", 2),
    Seed("不用了，謝謝", 2),
    Seed("你做得很好", 3, replies = listOf("謝謝")),
    Seed("你很認真", 3, replies = listOf("謝謝")),
    Seed("你畫得真好看", 3, replies = listOf("謝謝")),
    Seed("這個好酷", 3, replies = listOf("對啊", "謝謝")),
    Seed("很讚喔", 3, replies = listOf("謝謝"), caution = true),
    Seed("你想到好方法", 3, replies = listOf("謝謝")),
    Seed("你進步了", 3, replies = listOf("謝謝")),
    Seed("謝謝", 4, replies = listOf("不客氣")),
    Seed("不客氣", 4),
    Seed("謝謝你幫我", 4, replies = listOf("不客氣")),
    Seed("謝謝你分享", 4, replies = listOf("不客氣")),
    Seed("對不起", 4, replies = listOf("沒關係")),
    Seed("沒關係", 4),
    Seed("你人真好", 4, replies = listOf("謝謝")),
    Seed("加油", 5, replies = listOf("謝謝")),
    Seed("你可以的", 5, replies = listOf("謝謝")),
    Seed("你還好嗎", 5, replies = listOf("謝謝，我還好", "你難過嗎")),
    Seed("你難過嗎", 5, replies = listOf("我陪你")),
    Seed("我陪你", 5, replies = listOf("謝謝")),
    Seed("再試一次", 5, replies = listOf("好啊")),
    Seed("慢慢來", 5, replies = listOf("謝謝")),
    Seed("我相信你", 5, replies = listOf("謝謝")),
    Seed("一起玩好嗎", 6, replies = listOf("好啊", "等我一下", "下次再玩")),
    Seed("我們一組", 6, replies = listOf("好啊")),
    Seed("一起努力", 6, replies = listOf("好啊")),
    Seed("互相幫忙", 6),
    Seed("輪流玩", 6),
    Seed("我們做到了", 6),
    Seed("老師好", 7, teacherReplies = listOf("你好")),
    Seed("謝謝老師", 7),
    Seed("請幫我", 7, replies = listOf("好啊")),
    Seed("我不會", 7, replies = listOf("好啊", "請幫我")),
    Seed("我不喜歡這樣", 7, replies = listOf("對不起")),
    Seed("請不要這樣", 7, replies = listOf("對不起")),
    Seed("我要找老師", 7)
)

// 中年級（三、四年級）：語句完整、互惠關心、字數 10 字以內
private val MIDDLE = listOf(
    Seed("今天過得好嗎", 1, replies = listOf("很好啊", "有點累")),
    Seed("很好啊", 1),
    Seed("有點累", 1, replies = listOf("辛苦你囉")),
    Seed("等一下要做什麼", 1, replies = listOf("我想一下")),
    Seed("作業寫完了嗎", 1, replies = listOf("寫完了", "還沒寫完")),
    Seed("週末要去哪裡玩", 1, replies = listOf("我想一下")),
    Seed("很高興認識你", 1, replies = listOf("很高興認識你")),
    Seed("好久不見耶", 1, replies = listOf("好久不見耶")),
    Seed("寫完了", 2),
    Seed("還沒寫完", 2, replies = listOf("我們一起討論看看")),
    Seed("收到，謝謝你", 2),
    Seed("我想一下", 2),
    Seed("好主意", 2, replies = listOf("謝謝")),
    Seed("我有不同想法", 2, replies = listOf("我們一起討論看看")),
    Seed("下次再約", 2, replies = listOf("好啊")),
    Seed("你字寫得真好看", 3, replies = listOf("謝謝")),
    Seed("這張圖畫得太棒了", 3, replies = listOf("謝謝"), caution = true),
    Seed("這個點子真讚", 3, replies = listOf("謝謝")),
    Seed("你進步好多喔", 3, replies = listOf("謝謝")),
    Seed("你也是", 3),
    Seed("你好努力喔", 3, replies = listOf("謝謝")),
    Seed("真是個好幫手", 3, replies = listOf("謝謝")),
    Seed("真的太謝謝你了", 4, replies = listOf("不客氣")),
    Seed("辛苦你囉", 4, replies = listOf("謝謝")),
    Seed("沒關係，別放在心上", 4),
    Seed("多虧有你幫忙", 4, replies = listOf("不客氣")),
    Seed("謝謝你的分享", 4, replies = listOf("不客氣")),
    Seed("抱歉讓你久等了", 4, replies = listOf("沒關係，別放在心上")),
    Seed("對不起，是我不對", 4, replies = listOf("沒關係，別放在心上")),
    Seed("你還好嗎？需要幫忙嗎", 5, replies = listOf("我需要幫忙", "謝謝，我還好")),
    Seed("聽起來你很難過", 5, replies = listOf("謝謝")),
    Seed("相信自己，你可以的", 5, replies = listOf("謝謝")),
    Seed("沒關係，下次再來", 5, replies = listOf("謝謝")),
    Seed("你已經很努力了", 5, replies = listOf("謝謝")),
    Seed("有需要可以找我", 5, replies = listOf("謝謝")),
    Seed("別給自己太大壓力", 5, replies = listOf("謝謝")),
    Seed("失敗為成功之母", 5, replies = listOf("謝謝")),
    Seed("團結力量大", 6),
    Seed("我們是一隊的", 6),
    Seed("大家辛苦了", 6, replies = listOf("辛苦你囉")),
    Seed("合作愉快", 6, replies = listOf("合作愉快")),
    Seed("我們一起討論看看", 6, replies = listOf("好主意", "我有不同想法")),
    Seed("聽聽看大家的意見", 6),
    Seed("老師，我有問題", 7, teacherReplies = listOf("有問題隨時問老師")),
    Seed("老師，可以再說一次嗎", 7),
    Seed("收到，謝謝老師", 7),
    Seed("我需要幫忙", 7),
    Seed("請你不要這樣說", 7, replies = listOf("對不起，是我不對")),
    Seed("這樣讓我不舒服", 7, replies = listOf("對不起，是我不對")),
    Seed("我們去跟老師說", 7)
)

// 高年級（五、六年級）：同理陪伴、委婉協商、字數 14 字以內
private val UPPER = listOf(
    Seed("今天學校有發生什麼事嗎", 1, replies = listOf("就老樣子啊", "差不多了")),
    Seed("待會放學要不要一起走", 1, replies = listOf("好啊", "這次先不要，謝謝你約我")),
    Seed("這週末有什麼計畫嗎", 1, replies = listOf("讓我想想再回你")),
    Seed("考試準備得怎麼樣了", 1, replies = listOf("還在努力中", "差不多了")),
    Seed("祝你今天一切順利", 1, replies = listOf("謝謝")),
    Seed("有空再聊吧", 1, replies = listOf("好啊")),
    Seed("保持聯絡喔", 1, replies = listOf("保持聯絡喔")),
    Seed("很高興能跟你同班", 1, replies = listOf("你也是")),
    Seed("就老樣子啊", 2),
    Seed("還在努力中", 2, replies = listOf("加油")),
    Seed("差不多了", 2),
    Seed("我懂你的意思", 2),
    Seed("我想法不太一樣，可以說說嗎", 2, replies = listOf("好啊")),
    Seed("讓我想想再回你", 2),
    Seed("這次先不要，謝謝你約我", 2, replies = listOf("下次再約")),
    Seed("你這番話很有深度耶", 3, replies = listOf("沒有啦，你太誇獎了"), caution = true),
    Seed("令人刮目相看", 3, replies = listOf("沒有啦，你太誇獎了"), caution = true),
    Seed("這真的是實至名歸", 3, replies = listOf("沒有啦，你太誇獎了"), caution = true),
    Seed("一看就懂，你太會說明了", 3, replies = listOf("彼此彼此，互相學習")),
    Seed("你做事真的很有效率", 3, replies = listOf("謝謝")),
    Seed("你的努力大家都看到了", 3, replies = listOf("謝謝")),
    Seed("沒有啦，你太誇獎了", 3),
    Seed("謝謝你的提醒，我會注意", 4),
    Seed("感謝你的體諒", 4, replies = listOf("不客氣")),
    Seed("這次多虧有你帶大家", 4, replies = listOf("這是大家一起努力的成果")),
    Seed("謝謝你願意跟我說", 4),
    Seed("不好意思，剛才是我太衝動了", 4, replies = listOf("別放在心上，人都會失誤")),
    Seed("別放在心上，人都會失誤", 4),
    Seed("彼此彼此，互相學習", 4),
    Seed("懂你的感覺，這真的不容易", 5, replies = listOf("謝謝")),
    Seed("無論如何，我都在", 5, replies = listOf("謝謝")),
    Seed("你不是一個人", 5, replies = listOf("謝謝")),
    Seed("想哭就哭，沒關係", 5, replies = listOf("謝謝")),
    Seed("要不要我陪你去找老師", 5, replies = listOf("好啊", "我想一下")),
    Seed("這次挫折只是一時的", 5, replies = listOf("謝謝")),
    Seed("好好休息，身體最重要", 5, replies = listOf("謝謝")),
    Seed("我相信你能處理好", 5, replies = listOf("謝謝")),
    Seed("這是大家一起努力的成果", 6),
    Seed("我們彼此互補，一定可以", 6),
    Seed("既然有共識，就全力以赴吧", 6),
    Seed("謝謝大家尊重不同的聲音", 6),
    Seed("每個人都很重要", 6),
    Seed("勝不驕，敗不餒", 6),
    Seed("老師，這題想再請教您", 7),
    Seed("老師，我想跟您說一件事", 7, teacherReplies = listOf("好，老師在聽")),
    Seed("收到，謝謝老師提醒", 7),
    Seed("這種玩笑讓我不舒服，請停止", 7, replies = listOf("不好意思，剛才是我太衝動了")),
    Seed("我不想參與這種對話", 7),
    Seed("我覺得這樣不太好", 7),
    Seed("這件事應該讓老師知道", 7)
)

object DefaultVocabulary {
    fun all(): List<VocabEntry> =
        convert(GradeBand.LOWER, LOWER) + convert(GradeBand.MIDDLE, MIDDLE) + convert(GradeBand.UPPER, UPPER)

    private fun convert(band: GradeBand, seeds: List<Seed>): List<VocabEntry> =
        seeds.mapIndexed { index, seed ->
            VocabEntry(
                id = "${band.key}_${index + 1}",
                text = seed.text,
                category = seed.category,
                gradeBand = band,
                suggestions = seed.replies,
                teacherSuggestions = seed.teacherReplies,
                caution = seed.caution,
                enabled = true,
                order = index + 1
            )
        }
}
