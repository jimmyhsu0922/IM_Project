package com.example.improject.model

/** 二年級兼任社團老師的固定測試帳號設定。 */
object TeacherAssignments {
    private val secondGradeClubAssignments = mapOf(
        "T201" to "吉他社",
        "T202" to "熱音社",
        "T203" to "書法社",
        "T204" to "籃球社",
        "T205" to "遊戲開發社"
    )

    fun clubFor(teacherId: String): String = secondGradeClubAssignments[teacherId].orEmpty()
}
