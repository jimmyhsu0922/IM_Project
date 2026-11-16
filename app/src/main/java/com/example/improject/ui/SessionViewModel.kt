package com.example.improject.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.improject.data.Accounts
import com.example.improject.data.AuthRepository
import com.example.improject.data.UserRepository
import com.example.improject.data.toFriendlyMessage
import com.example.improject.domain.AppUser
import com.example.improject.domain.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class SignedIn(val user: AppUser) : SessionState
}

/** 登入狀態。重開 App 時若先前已登入，會直接回到對應的畫面。 */
class SessionViewModel : ViewModel() {
    private val auth = AuthRepository()
    private val users = UserRepository()

    private val _state = MutableStateFlow<SessionState>(SessionState.Loading)
    val state: StateFlow<SessionState> = _state

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy

    /** 每次登入失敗加一；登入畫面看到數字變了就清空密碼欄。 */
    private val _failureCount = MutableStateFlow(0)
    val failureCount: StateFlow<Int> = _failureCount

    private fun fail(message: String) {
        _error.value = message
        _failureCount.value = _failureCount.value + 1
    }

    init {
        restore()
    }

    private fun restore() {
        val uid = auth.currentUid
        if (uid == null) {
            _state.value = SessionState.SignedOut
            return
        }
        viewModelScope.launch {
            try {
                val user = users.getUser(uid)
                if (user != null && user.active) {
                    _state.value = SessionState.SignedIn(user)
                } else {
                    auth.signOut()
                    _state.value = SessionState.SignedOut
                }
            } catch (e: Exception) {
                _error.value = e.toFriendlyMessage()
                _state.value = SessionState.SignedOut
            }
        }
    }

    fun login(account: String, password: String) {
        if (account.isBlank() || password.isBlank()) {
            fail("請輸入帳號和密碼")
            return
        }
        if (!Accounts.isValid(account)) {
            fail("帳號只能使用英文、數字")
            return
        }
        _busy.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val uid = auth.signIn(account, password)
                var user = users.getUser(uid)
                if (user == null && Accounts.isAdmin(account)) {
                    // 第一次用管理員帳號登入：自動建立管理員資料（安全規則只允許這個帳號這麼做）。
                    user = AppUser(
                        uid = uid,
                        account = Accounts.ADMIN_ACCOUNT,
                        realName = "系統管理員",
                        role = Role.ADMIN
                    )
                    users.save(user)
                }
                when {
                    user == null -> {
                        auth.signOut()
                        fail("此帳號尚未開通，請聯絡管理員")
                    }
                    !user.active -> {
                        auth.signOut()
                        fail("此帳號已停用，請聯絡管理員")
                    }
                    else -> _state.value = SessionState.SignedIn(user)
                }
            } catch (e: Exception) {
                auth.signOut()
                fail(e.toFriendlyMessage())
            } finally {
                _busy.value = false
            }
        }
    }

    fun logout() {
        // 先切換畫面狀態，再登出。畫面離開時會一併停止該帳號的所有資料監聽。
        _error.value = null
        _state.value = SessionState.SignedOut
        auth.signOut()
    }
}
