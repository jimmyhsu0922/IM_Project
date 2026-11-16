package com.example.improject.data

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException

/** 登入、登出與建立帳號（Firebase Authentication）。 */
class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUid: String?
        get() = auth.currentUser?.uid

    suspend fun signIn(account: String, password: String): String {
        val result = auth.signInWithEmailAndPassword(Accounts.toEmail(account), password).awaitResult()
        return result.user?.uid ?: throw IllegalStateException("登入失敗")
    }

    fun signOut() {
        auth.signOut()
    }

    /** 管理員替別人建立帳號，回傳新帳號的 uid。帳號已存在時丟出例外。 */
    suspend fun createAccount(account: String, password: String): String {
        val secondary = secondaryAuth()
        try {
            val result = secondary
                .createUserWithEmailAndPassword(Accounts.toEmail(account), password)
                .awaitResult()
            return result.user?.uid ?: throw IllegalStateException("無法取得帳號代碼")
        } finally {
            secondary.signOut()
        }
    }

    /** 建立帳號；若已存在就用同一組密碼登入取得 uid（給示範資料重複執行用）。 */
    suspend fun createOrFindAccount(account: String, password: String): String {
        val secondary = secondaryAuth()
        val email = Accounts.toEmail(account)
        try {
            val result = try {
                secondary.createUserWithEmailAndPassword(email, password).awaitResult()
            } catch (e: FirebaseAuthUserCollisionException) {
                secondary.signInWithEmailAndPassword(email, password).awaitResult()
            }
            return result.user?.uid ?: throw IllegalStateException("無法取得帳號代碼")
        } finally {
            secondary.signOut()
        }
    }

    /**
     * 用第二個 FirebaseApp 建立帳號。
     * 直接用主要的 FirebaseAuth 建立帳號會把目前登入的管理員換成新帳號，
     * 所以另開一個獨立的連線，管理員的登入狀態不受影響。
     */
    private fun secondaryAuth(): FirebaseAuth {
        val primary = FirebaseApp.getInstance()
        val app = try {
            FirebaseApp.getInstance(SECONDARY_APP)
        } catch (e: IllegalStateException) {
            FirebaseApp.initializeApp(primary.applicationContext, primary.options, SECONDARY_APP)
        }
        return FirebaseAuth.getInstance(app)
    }

    private companion object {
        const val SECONDARY_APP = "account-creator"
    }
}
