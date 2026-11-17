package com.example.improject.data.seed

import com.example.improject.data.AuthRepository
import com.example.improject.data.SchoolRepository
import com.example.improject.data.UserRepository
import com.example.improject.data.VocabularyRepository
import com.example.improject.data.toFriendlyMessage
import com.example.improject.domain.AppUser
import com.example.improject.domain.Role

/**
 * 校內示範用的資料：3 個班級、5 個社團、預設句庫、3 位老師與 12 位學生。
 * 可以重複執行，已存在的帳號會直接更新資料。
 * 帳號數量刻意保持很少，因為 Firebase 對同一來源每小時能建立的帳號數有限制。
 */
class DemoDataSeeder(
    private val auth: AuthRepository,
    private val users: UserRepository,
    private val school: SchoolRepository,
    private val vocabulary: VocabularyRepository
) {
    private data class DemoAccount(
        val account: String,
        val name: String,
        val role: Role,
        val grade: Int,
        val classId: String,
        val clubId: String
    )

    suspend fun seed(log: (String) -> Unit) {
        log("建立班級與社團…")
        DEMO_GRADES.forEach { grade -> school.addClass(grade, 1, SCHOOL_YEAR) }
        DEMO_CLUBS.forEach { (id, name) -> school.addClub(id, name) }

        log("匯入預設句庫…")
        val count = vocabulary.importDefaults()
        log("已匯入 $count 句")

        log("建立帳號（密碼皆為 $DEMO_PASSWORD）…")
        demoAccounts().forEach { demo ->
            try {
                val uid = auth.createOrFindAccount(demo.account, DEMO_PASSWORD)
                users.save(
                    AppUser(
                        uid = uid,
                        account = demo.account,
                        realName = demo.name,
                        role = demo.role,
                        grade = demo.grade,
                        classId = demo.classId,
                        clubId = demo.clubId,
                        active = true
                    )
                )
                log("✓ ${demo.account}　${demo.name}")
            } catch (e: Exception) {
                log("✗ ${demo.account}：${e.toFriendlyMessage()}")
            }
        }
        log("完成")
    }

    private fun demoAccounts(): List<DemoAccount> {
        val teacherNames = listOf("陳怡君", "林志明", "黃淑芬")
        val teacherClubs = listOf("club-guitar", "club-band", "")
        val studentNames = listOf(
            listOf("王小明", "李小華", "張小美", "陳小安"),
            listOf("林子晴", "吳承恩", "周品妍", "許家豪"),
            listOf("蔡宜庭", "鄭宇翔", "謝欣怡", "郭柏翰")
        )
        val result = mutableListOf<DemoAccount>()
        DEMO_GRADES.forEachIndexed { index, grade ->
            val classId = SchoolRepository.classId(grade, 1)
            result += DemoAccount(
                account = "T${grade}01",
                name = teacherNames[index],
                role = Role.TEACHER,
                grade = 0,
                classId = classId,
                clubId = teacherClubs[index]
            )
            studentNames[index].forEachIndexed { seatIndex, name ->
                val seat = seatIndex + 1
                result += DemoAccount(
                    account = "%d01%02d".format(grade, seat),
                    name = name,
                    role = Role.STUDENT,
                    grade = grade,
                    classId = classId,
                    clubId = if (seat % 2 == 1) "club-guitar" else "club-band"
                )
            }
        }
        return result
    }

    companion object {
        const val DEMO_PASSWORD = "demo1234"
        const val SCHOOL_YEAR = "115"
        val DEMO_GRADES = listOf(1, 3, 5)
        val DEMO_CLUBS = listOf(
            "club-guitar" to "吉他社",
            "club-band" to "熱音社",
            "club-calligraphy" to "書法社",
            "club-basketball" to "籃球社",
            "club-gamedev" to "遊戲開發社"
        )
    }
}
