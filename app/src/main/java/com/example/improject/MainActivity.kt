package com.example.improject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.improject.model.StudentChatScreen
import com.example.improject.model.TeacherChatScreen
import com.example.improject.ui.theme.IMProjectTheme
import com.example.improject.ui.theme.RoleSelectionScreen
import com.example.improject.model.TestDataManager

enum class AppScreen {
    ROLE_SELECTION, LOGIN, REGISTER, CHAT
}

data class UserSession(
    val myId: String = "",
    val realName: String = "",
    val className: String = "",
    val clubName: String = "",
    val teacherGroup: String = ""
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 💡 在 App 一啟動時，直接呼叫獨立檔案裡的造資料功能
        // TestDataManager.generateTestData()

        enableEdgeToEdge()
        setContent {
            IMProjectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf(AppScreen.ROLE_SELECTION) }
                    var isStudent by remember { mutableStateOf(true) }
                    var userSession by remember { mutableStateOf(UserSession()) }

                    when (currentScreen) {
                        AppScreen.ROLE_SELECTION -> {
                            RoleSelectionScreen(
                                onRoleSelected = { studentRole ->
                                    isStudent = studentRole
                                    currentScreen = AppScreen.LOGIN
                                }
                            )
                        }
                        AppScreen.LOGIN -> {
                            AuthScreen(
                                title = if (isStudent) "學生登入" else "老師登入",
                                actionName = "登入",
                                isLogin = true,
                                isStudent = isStudent,
                                onSuccess = { myId, realName, className, clubName, teacherGroup ->
                                    userSession = UserSession(myId, realName, className, clubName, teacherGroup)
                                    currentScreen = AppScreen.CHAT
                                },
                                onSwitchMode = { currentScreen = AppScreen.REGISTER },
                                onBack = { currentScreen = AppScreen.ROLE_SELECTION }
                            )
                        }
                        AppScreen.REGISTER -> {
                            AuthScreen(
                                title = if (isStudent) "學生註冊" else "老師註冊",
                                actionName = "註冊",
                                isLogin = false,
                                isStudent = isStudent,
                                onSuccess = { myId, realName, className, clubName, teacherGroup ->
                                    userSession = UserSession(myId, realName, className, clubName, teacherGroup)
                                    currentScreen = AppScreen.CHAT
                                },
                                onSwitchMode = { currentScreen = AppScreen.LOGIN },
                                onBack = { currentScreen = AppScreen.ROLE_SELECTION }
                            )
                        }
                        AppScreen.CHAT -> {
                            // 💡 根據登入身份，分流導向完全不同的獨立 Compose 檔案
                            if (isStudent) {
                                StudentChatScreen(
                                    userSession = userSession,
                                    onBack = { currentScreen = AppScreen.ROLE_SELECTION }
                                )
                            } else {
                                TeacherChatScreen(
                                    userSession = userSession,
                                    onBack = { currentScreen = AppScreen.ROLE_SELECTION }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}