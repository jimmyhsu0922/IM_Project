package com.example.improject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.improject.model.MenuDrivenChatScreen
import com.example.improject.ui.theme.IMProjectTheme
import com.example.improject.ui.theme.RoleSelectionScreen

enum class AppScreen {
    ROLE_SELECTION, LOGIN, REGISTER, CHAT
}

// 建立一個資料類別來暫存使用者的群組資訊
data class UserSession(
    val className: String = "",
    val clubName: String = "",
    val teacherGroup: String = ""
)

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
                    var currentScreen by remember { mutableStateOf(AppScreen.ROLE_SELECTION) }
                    var isStudentRole by remember { mutableStateOf(true) }
                    var userSession by remember { mutableStateOf(UserSession()) }

                    when (currentScreen) {
                        AppScreen.ROLE_SELECTION -> {
                            // 假設你有 RoleSelectionScreen
                            RoleSelectionScreen(
                                onRoleSelected = { selectedRole ->
                                    isStudentRole = selectedRole
                                    currentScreen = AppScreen.LOGIN
                                }
                            )
                        }
                        AppScreen.LOGIN, AppScreen.REGISTER -> {
                            AuthScreen(
                                isLogin = currentScreen == AppScreen.LOGIN,
                                isStudent = isStudentRole,
                                onSuccess = { className, clubName, teacherGroup ->
                                    // 登入或註冊成功後，儲存群組資訊並進入聊天室
                                    userSession = UserSession(className, clubName, teacherGroup)
                                    currentScreen = AppScreen.CHAT
                                },
                                onSwitchMode = {
                                    currentScreen = if (currentScreen == AppScreen.LOGIN) AppScreen.REGISTER else AppScreen.LOGIN
                                },
                                onBack = { currentScreen = AppScreen.ROLE_SELECTION }
                            )
                        }
                        AppScreen.CHAT -> {
                            MenuDrivenChatScreen(
                                isStudent = isStudentRole,
                                userSession = userSession,
                                onBackClick = { currentScreen = AppScreen.ROLE_SELECTION }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuthScreen(
    isLogin: Boolean,
    isStudent: Boolean,
    onSuccess: (String, String, String) -> Unit,
    onSwitchMode: () -> Unit,
    onBack: () -> Unit
) {
    var accountId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // 新增：班級與社團的狀態
    var className by remember { mutableStateOf("") }
    var clubName by remember { mutableStateOf("") }
    var teacherGroup by remember { mutableStateOf("") }

    val roleName = if (isStudent) "學生" else "老師"
    val actionName = if (isLogin) "登入" else "註冊"
    val accountLabel = if (isStudent) "學號" else "帳號"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "$roleName $actionName", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = accountId,
            onValueChange = { accountId = it },
            label = { Text("請輸入$accountLabel") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("請輸入密碼") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 註冊時需要輸入群組資訊
        if (!isLogin) {
            if (isStudent) {
                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text("請輸入班級 (例: 三年二班)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = clubName,
                    onValueChange = { clubName = it },
                    label = { Text("請輸入社團 (例: 籃球社)") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedTextField(
                    value = teacherGroup,
                    onValueChange = { teacherGroup = it },
                    label = { Text("輸入負責班級或社團") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onSuccess(className, clubName, teacherGroup) },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(actionName, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onSwitchMode) {
            Text(if (isLogin) "沒有帳號？點此註冊" else "已有帳號？點此登入")
        }

        TextButton(onClick = onBack) {
            Text("返回重新選擇身分")
        }
    }
}