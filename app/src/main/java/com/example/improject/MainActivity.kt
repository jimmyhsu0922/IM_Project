package com.example.improject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.improject.model.MenuDrivenChatScreen
import com.example.improject.ui.theme.IMProjectTheme
import com.example.improject.ui.theme.RoleSelectionScreen

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
                    // 使用 nullable Boolean 來記錄狀態
                    // null = 還沒選擇, true = 學生, false = 老師
                    var isStudentRole by remember { mutableStateOf<Boolean?>(null) }

                    if (isStudentRole == null) {
                        // 如果狀態是 null，顯示身分選擇畫面
                        RoleSelectionScreen(
                            onRoleSelected = { selectedRole ->
                                isStudentRole = selectedRole // 接收選擇結果並更新狀態
                            }
                        )
                    } else {
                        // 已經選擇身分，進入聊天畫面
                        MenuDrivenChatScreen(
                            isStudent = isStudentRole!!,
                            onBackClick = {
                                // 點擊左上角返回按鈕時，將狀態重設為 null，就會回到選擇畫面
                                isStudentRole = null
                            }
                        )
                    }
                }
            }
        }
    }
}