package com.example.improject.data

import com.example.improject.domain.AppUser
import com.example.improject.domain.Role
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow

/** users 集合：使用者的姓名、角色、年級、所屬班級與社團。密碼不存在這裡。 */
class UserRepository {
    private val users = FirebaseFirestore.getInstance().collection("users")

    suspend fun getUser(uid: String): AppUser? {
        val doc = users.document(uid).get().awaitResult()
        return if (doc.exists()) doc.toAppUser() else null
    }

    fun observeAll(): Flow<List<AppUser>> = users.asFlow { it.toAppUser() }

    suspend fun save(user: AppUser) {
        val data = mapOf(
            "account" to user.account,
            "realName" to user.realName,
            "role" to user.role.key,
            "grade" to user.grade,
            "classId" to user.classId,
            "clubId" to user.clubId,
            "active" to user.active
        )
        users.document(user.uid).set(data).awaitResult()
    }
}

private fun DocumentSnapshot.toAppUser(): AppUser = AppUser(
    uid = id,
    account = getString("account").orEmpty(),
    realName = getString("realName").orEmpty(),
    role = Role.fromKey(getString("role")),
    grade = (getLong("grade") ?: 0L).toInt(),
    classId = getString("classId").orEmpty(),
    clubId = getString("clubId").orEmpty(),
    active = getBoolean("active") ?: true
)
