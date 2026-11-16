package com.example.improject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.improject.domain.Role
import com.example.improject.ui.SessionState
import com.example.improject.ui.SessionViewModel
import com.example.improject.ui.admin.AdminScreen
import com.example.improject.ui.auth.LoginScreen
import com.example.improject.ui.student.StudentChatScreen
import com.example.improject.ui.teacher.TeacherChatScreen
import com.example.improject.ui.theme.IMProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IMProjectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRoot()
                }
            }
        }
    }
}

/**
 * 一次登入期間專用的 ViewModel 容器。
 * 登出時整個清掉，聊天室與後台的資料監聽會跟著停止，
 * 下一位登入的人（即使是同一個帳號）拿到的是全新的狀態。
 */
private class LoginSessionStoreOwner : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
}

/** 依登入狀態與角色決定顯示哪個畫面。 */
@Composable
fun AppRoot(sessionViewModel: SessionViewModel = viewModel()) {
    val state by sessionViewModel.state.collectAsState()
    val error by sessionViewModel.error.collectAsState()
    val busy by sessionViewModel.busy.collectAsState()
    val failureCount by sessionViewModel.failureCount.collectAsState()

    when (val current = state) {
        SessionState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        SessionState.SignedOut -> {
            LoginScreen(
                busy = busy,
                error = error,
                failureCount = failureCount,
                onLogin = { account, password -> sessionViewModel.login(account, password) }
            )
        }
        is SessionState.SignedIn -> {
            val user = current.user
            val onLogout = { sessionViewModel.logout() }
            key(user.uid) {
                val storeOwner = remember { LoginSessionStoreOwner() }
                DisposableEffect(storeOwner) {
                    onDispose { storeOwner.viewModelStore.clear() }
                }
                CompositionLocalProvider(LocalViewModelStoreOwner provides storeOwner) {
                    when (user.role) {
                        Role.STUDENT -> StudentChatScreen(user = user, onLogout = onLogout)
                        Role.TEACHER -> TeacherChatScreen(user = user, onLogout = onLogout)
                        Role.ADMIN -> AdminScreen(user = user, onLogout = onLogout)
                    }
                }
            }
        }
    }
}
