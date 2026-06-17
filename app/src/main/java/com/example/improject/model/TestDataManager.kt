package com.example.improject.model // 💡 請根據你實際的 package 位置修改

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch

object TestDataManager {

    /**
     * 💡 專門用來批次產生 240位學生 與 48位老師 的測試資料
     */
    fun generateTestData() {
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        val years = listOf("一年", "二年", "三年", "四年", "五年", "六年")
        val classes = listOf("一班", "二班", "三班", "四班", "五班", "六班", "七班", "八班")
        val clubs = listOf("吉他社", "熱音社", "書法社", "籃球社", "遊戲開發社")

        // 1. 開始跑迴圈：6 個年級
        for (yIdx in 0..5) {
            val yearNum = yIdx + 1 // 數字 1~6
            val yearName = years[yIdx] // "一年"

            // 2. 跑迴圈：8 個班級
            for (cIdx in 0..7) {
                val classNum = cIdx + 1 // 數字 1~8
                val className = classes[cIdx] // "一班"

                // 拼接出班級群組名稱（例：一年一班）
                val finalClassName = "$yearName$className"

                // ================= 建立老師資料 =================
                // 帳號格式：年(1碼) + 班(2碼) + 10 (例：一年一班老師為 10110)
                val teacherId = String.format("T%d%02d", yearNum, classNum)
                val teacherDocRef = db.collection("users").document(teacherId)

                val teacherData = hashMapOf(
                    "username" to teacherId,
                    "password" to teacherId, // 密碼跟帳號一樣方便測試
                    "realName" to "${finalClassName}老師",
                    "isStudent" to false,
                    "className" to "",
                    "clubName" to "",
                    "teacherGroup" to finalClassName
                )
                batch.set(teacherDocRef, teacherData)

                // ================= 建立 5 位學生資料 =================
                // 3. 跑迴圈：5 位學生
                for (sNum in 1..5) {
                    // 帳號格式：年(1碼) + 班(2碼) + 座號(2碼) (例：10101)
                    val studentId = String.format("%d%02d%02d", yearNum, classNum, sNum)
                    val studentDocRef = db.collection("users").document(studentId)

                    // 隨機發配一個社團
                    val randomClub = clubs[(sNum - 1) % clubs.size]

                    val studentData = hashMapOf(
                        "username" to studentId,
                        "password" to studentId, // 密碼跟帳號一樣方便測試
                        "realName" to "${finalClassName}${sNum}號同學",
                        "isStudent" to true,
                        "className" to finalClassName,
                        "clubName" to randomClub,
                        "teacherGroup" to ""
                    )
                    batch.set(studentDocRef, studentData)
                }
            }
        }

        // 🔥 4. 將全部打包好的資料一次送上雲端
        batch.commit()
            .addOnSuccessListener {
                println("====== 🎉 240位學生與48位老師資料批次建立成功！ ======")
            }
            .addOnFailureListener { e ->
                println("====== ❌ 建立失敗: ${e.message} ======")
            }
    }
}