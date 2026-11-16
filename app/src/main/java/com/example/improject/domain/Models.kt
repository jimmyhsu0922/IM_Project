package com.example.improject.domain

/** 使用者角色。key 是存在 Firestore 的值，安全規則也用同一組字串判斷。 */
enum class Role(val key: String, val label: String) {
    STUDENT("student", "學生"),
    TEACHER("teacher", "老師"),
    ADMIN("admin", "管理員");

    companion object {
        fun fromKey(key: String?): Role = entries.firstOrNull { it.key == key } ?: STUDENT
    }
}

/** 年段：決定學生使用哪一套句庫。三個年段的分類名稱相同。 */
enum class GradeBand(val key: String, val label: String) {
    LOWER("lower", "低年級"),
    MIDDLE("middle", "中年級"),
    UPPER("upper", "高年級");

    fun categoryName(category: Int): String = CATEGORY_LABELS.getOrElse(category - 1) { "其他" }

    companion object {
        fun fromKey(key: String?): GradeBand = entries.firstOrNull { it.key == key } ?: LOWER

        fun forGrade(grade: Int): GradeBand = when (grade) {
            3, 4 -> MIDDLE
            5, 6 -> UPPER
            else -> LOWER
        }
    }
}

/** 句子分類共七類（1～7）。 */
val CATEGORY_LABELS = listOf("問候與日常", "回應與確認", "讚美與肯定", "感謝與道歉", "關心與鼓勵", "合作與團隊", "求助與師生")
const val CATEGORY_COUNT = 7

/** users/{uid} */
data class AppUser(
    val uid: String = "",
    val account: String = "",
    val realName: String = "",
    val role: Role = Role.STUDENT,
    val grade: Int = 0,
    val classId: String = "",
    val clubId: String = "",
    val active: Boolean = true
) {
    val gradeBand: GradeBand get() = GradeBand.forGrade(grade)
}

/** classes/{classId} */
data class SchoolClass(
    val id: String = "",
    val name: String = "",
    val grade: Int = 0,
    val schoolYear: String = ""
)

/** clubs/{clubId} */
data class Club(
    val id: String = "",
    val name: String = ""
)

/** rooms/{roomId} 的 announcement 欄位 */
data class Announcement(
    val text: String = "",
    val senderName: String = "",
    val messageId: String = ""
)

/** rooms/{roomId}：聊天室代碼與班級或社團代碼相同。 */
data class Room(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val announcement: Announcement? = null
)

/** rooms/{roomId}/messages/{messageId} */
data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderRole: Role = Role.STUDENT,
    val vocabId: String = "",
    val category: Int = 0,
    val timestamp: Long = 0L,
    val edited: Boolean = false
)

/** vocabulary/{vocabId} */
data class VocabEntry(
    val id: String = "",
    val text: String = "",
    val category: Int = 1,
    val gradeBand: GradeBand = GradeBand.LOWER,
    /** 同學收到這句時，推薦給同學的回應。 */
    val suggestions: List<String> = emptyList(),
    /** 老師收到這句時，推薦給老師的回應。 */
    val teacherSuggestions: List<String> = emptyList(),
    /** 這句容易被拿來反諷，後台會標示提醒。 */
    val caution: Boolean = false,
    val enabled: Boolean = true,
    val order: Int = 0
)
