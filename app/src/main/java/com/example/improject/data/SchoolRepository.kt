package com.example.improject.data

import com.example.improject.domain.Club
import com.example.improject.domain.SchoolClass
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow

/** classes 與 clubs 集合。建立班級或社團時，同時建立同代碼的聊天室（rooms）。 */
class SchoolRepository {
    private val db = FirebaseFirestore.getInstance()

    fun observeClasses(): Flow<List<SchoolClass>> = db.collection("classes").asFlow { doc ->
        SchoolClass(
            id = doc.id,
            name = doc.getString("name").orEmpty(),
            grade = (doc.getLong("grade") ?: 0L).toInt(),
            schoolYear = doc.getString("schoolYear").orEmpty()
        )
    }

    fun observeClubs(): Flow<List<Club>> = db.collection("clubs").asFlow { doc ->
        Club(id = doc.id, name = doc.getString("name").orEmpty())
    }

    suspend fun addClass(grade: Int, number: Int, schoolYear: String): SchoolClass {
        val schoolClass = SchoolClass(
            id = classId(grade, number),
            name = className(grade, number),
            grade = grade,
            schoolYear = schoolYear
        )
        val batch = db.batch()
        batch.set(
            db.collection("classes").document(schoolClass.id),
            mapOf("name" to schoolClass.name, "grade" to grade, "schoolYear" to schoolYear)
        )
        batch.set(
            db.collection("rooms").document(schoolClass.id),
            mapOf("name" to schoolClass.name, "type" to "class"),
            SetOptions.merge()
        )
        batch.commit().awaitResult()
        return schoolClass
    }

    /** id 傳空字串時由 Firestore 自動產生代碼。 */
    suspend fun addClub(id: String, name: String): Club {
        val ref = if (id.isBlank()) db.collection("clubs").document() else db.collection("clubs").document(id)
        val batch = db.batch()
        batch.set(ref, mapOf("name" to name))
        batch.set(
            db.collection("rooms").document(ref.id),
            mapOf("name" to name, "type" to "club"),
            SetOptions.merge()
        )
        batch.commit().awaitResult()
        return Club(id = ref.id, name = name)
    }

    companion object {
        val GRADE_NAMES = listOf("一年", "二年", "三年", "四年", "五年", "六年")
        val CLASS_NAMES = listOf("一班", "二班", "三班", "四班", "五班", "六班", "七班", "八班")

        /** 例：一年一班 → class-101 */
        fun classId(grade: Int, number: Int): String = "class-%d%02d".format(grade, number)

        fun className(grade: Int, number: Int): String =
            GRADE_NAMES.getOrElse(grade - 1) { "${grade}年" } + CLASS_NAMES.getOrElse(number - 1) { "${number}班" }
    }
}
