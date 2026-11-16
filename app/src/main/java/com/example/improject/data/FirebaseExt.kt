package com.example.improject.data

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** 把 Firebase 的 Task 轉成 suspend 呼叫。 */
suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { task ->
        if (!cont.isActive) return@addOnCompleteListener
        val error = task.exception
        if (error != null) {
            cont.resumeWithException(error)
        } else if (task.isCanceled) {
            cont.cancel()
        } else {
            cont.resume(task.result)
        }
    }
}

/** 把查詢轉成會即時更新的 Flow；停止收集時自動移除監聽。 */
internal fun <T> Query.asFlow(mapper: (DocumentSnapshot) -> T): Flow<List<T>> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            close(error)
            return@addSnapshotListener
        }
        if (snapshot != null) {
            trySend(snapshot.documents.map(mapper))
        }
    }
    awaitClose { registration.remove() }
}

/** 把例外轉成給使用者看的中文訊息。 */
fun Throwable.toFriendlyMessage(): String = when (this) {
    is FirebaseAuthWeakPasswordException -> "密碼至少需要 6 個字元"
    is FirebaseAuthInvalidUserException -> "帳號或密碼錯誤"
    is FirebaseAuthInvalidCredentialsException -> "帳號或密碼錯誤"
    is FirebaseAuthUserCollisionException -> "此帳號已經存在"
    is FirebaseNetworkException -> "無法連線，請檢查網路"
    is FirebaseFirestoreException ->
        if (code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
            "沒有權限執行這個動作"
        } else {
            "資料庫錯誤：${message ?: code.name}"
        }
    else -> message ?: "發生未知錯誤"
}

/**
 * 學號／工號與 Firebase Authentication 電子郵件的對應。
 * 小學生沒有信箱，所以登入時輸入帳號，內部轉成「帳號@固定網域」。
 * ADMIN_ACCOUNT 與 EMAIL_DOMAIN 必須和 firestore.rules 裡的管理員信箱一致。
 */
object Accounts {
    const val EMAIL_DOMAIN = "xiaoshentong.example.com"
    const val ADMIN_ACCOUNT = "admin"

    private val pattern = Regex("^[a-z0-9._-]{2,30}$")

    fun normalize(account: String): String = account.trim().lowercase()

    fun toEmail(account: String): String = "${normalize(account)}@$EMAIL_DOMAIN"

    fun isValid(account: String): Boolean = pattern.matches(normalize(account))

    fun isAdmin(account: String): Boolean = normalize(account) == ADMIN_ACCOUNT
}
