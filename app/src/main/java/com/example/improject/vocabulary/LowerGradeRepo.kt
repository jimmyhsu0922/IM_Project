package com.example.improject.vocabulary

object LowerGradeRepo {
    val vocabularyList = listOf(
        // === 1. 社交問候與日常互動 (category = 1) ===
        Vocabulary("你好呀", 1, 1, listOf("你好呀", "早安")),
        Vocabulary("早安", 1, 2, listOf("早安")),
        Vocabulary("拜拜", 1, 3, listOf("拜拜", "明天見")),
        Vocabulary("明天見", 1, 4, listOf("明天見", "拜拜")),
        Vocabulary("我們一起玩", 1, 5, listOf("好啊", "等我一下")),
        Vocabulary("好啊", 1, 6, listOf("我們一起玩")),
        Vocabulary("等我一下", 1, 7, listOf("我們一起玩")),
        Vocabulary("你在做什麼", 1, 8, listOf("我在玩", "我在寫功課")),
        Vocabulary("放學了", 1, 9, listOf("明天見", "拜拜")),
        Vocabulary("生日快樂", 1, 10, listOf("謝謝")),

        // === 2. 讚美與正面肯定 (category = 2) ===
        Vocabulary("你好棒", 2, 11, listOf("謝謝")),
        Vocabulary("太厲害了", 2, 12, listOf("謝謝")),
        Vocabulary("這個超酷", 2, 13, listOf("對啊", "謝謝")),
        Vocabulary("真好看", 2, 14, listOf("謝謝")),
        Vocabulary("你好聰明", 2, 15, listOf("沒有啦", "謝謝")),
        Vocabulary("對啊", 2, 16, listOf("沒錯")),
        Vocabulary("沒錯", 2, 17, listOf("對啊")),
        Vocabulary("很讚喔", 2, 18, listOf("謝謝")),
        Vocabulary("好漂亮", 2, 19, listOf("謝謝")),
        Vocabulary("第一名", 2, 20, listOf("太厲害了", "加油")),

        // === 3. 感恩與禮貌回應 (category = 3) ===
        Vocabulary("謝謝", 3, 21, listOf("不客氣", "不用客氣")),
        Vocabulary("不客氣", 3, 22, listOf("謝謝")),
        Vocabulary("謝謝你的幫忙", 3, 23, listOf("不客氣", "你人真好")),
        Vocabulary("沒關係", 3, 24, listOf("對不起")),
        Vocabulary("對不起", 3, 25, listOf("沒關係")),
        Vocabulary("請幫我", 3, 26, listOf("好啊", "等我一下")),
        Vocabulary("好謝謝", 3, 27, listOf("不客氣")),
        Vocabulary("麻煩你了", 3, 28, listOf("不用客氣", "沒關係")),
        Vocabulary("你人真好", 3, 29, listOf("謝謝", "不客氣")),
        Vocabulary("不用客氣", 3, 30, listOf("謝謝")),

        // === 4. 鼓勵、打氣與關懷 (category = 4) ===
        Vocabulary("加油", 4, 31, listOf("好啊", "謝謝")),
        Vocabulary("你可以的", 4, 32, listOf("加油", "謝謝")),
        Vocabulary("不要哭", 4, 33, listOf("不哭不哭", "沒事沒事")),
        Vocabulary("不哭不哭", 4, 34, listOf("沒事沒事", "別傷心")),
        Vocabulary("再試一次", 4, 35, listOf("加油", "你可以的")),
        Vocabulary("沒事沒事", 4, 36, listOf("謝謝", "你人真好")),
        Vocabulary("你是最棒的", 4, 37, listOf("謝謝", "我相信你")),
        Vocabulary("別傷心", 4, 38, listOf("不哭不哭", "沒事沒事")),
        Vocabulary("慢慢來", 4, 39, listOf("好", "等我一下")),
        Vocabulary("我相信你", 4, 40, listOf("謝謝", "加油")),

        // === 5. 合作與團隊認同 (category = 5) ===
        Vocabulary("我們一組", 5, 41, listOf("一起努力", "好夥伴")),
        Vocabulary("一起努力", 5, 42, listOf("大家加油", "我們做到了")),
        Vocabulary("互相幫忙", 5, 43, listOf("一起努力", "好夥伴")),
        Vocabulary("我們贏了", 5, 44, listOf("我們做到了", "第一名")),
        Vocabulary("大家加油", 5, 45, listOf("一起努力", "我們一組")),
        Vocabulary("好夥伴", 5, 46, listOf("我們一組", "互相幫忙")),
        Vocabulary("一起排隊", 5, 47, listOf("好啊", "等我一下")),
        Vocabulary("我們做到了", 5, 48, listOf("我們贏了", "太厲害了")),
        Vocabulary("聽你的", 5, 49, listOf("謝謝", "一起努力")),
        Vocabulary("一起收玩具", 5, 50, listOf("好啊", "等我一下"))
    )
}