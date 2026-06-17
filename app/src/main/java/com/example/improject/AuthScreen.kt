package com.example.improject

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    title: String,
    actionName: String,
    isLogin: Boolean,
    isStudent: Boolean,
    onSuccess: (String, String, String, String, String) -> Unit,
    onSwitchMode: () -> Unit,
    onBack: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var realName by remember { mutableStateOf("") }

    // 💡 班級改為下拉選單的狀態控制
    val years = listOf("一年", "二年", "三年", "四年", "五年", "六年")
    val classes = listOf("一班", "二班", "三班", "四班", "五班", "六班", "七班", "八班")
    val clubs = listOf("吉他社", "熱音社", "書法社", "籃球社", "遊戲開發社")

    var selectedYear by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("") }
    var selectedClub by remember { mutableStateOf("") }
    var teacherGroup by remember { mutableStateOf("") }

    // 控制下拉選單展開/收合的狀態
    var yearExpanded by remember { mutableStateOf(false) }
    var classExpanded by remember { mutableStateOf(false) }
    var clubExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("帳號 (學號/工號)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("密碼") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (!isLogin) {
            OutlinedTextField(
                value = realName,
                onValueChange = { realName = it },
                label = { Text("使用者真實姓名 (例: 王小明)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (isStudent) {
                Text("請選擇您的班級：", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))

                // 💡 橫向排列：左邊選年級、右邊選班級
                Row(modifier = Modifier.fillMaxWidth()) {
                    // 年級下拉選單
                    ExposedDropdownMenuBox(
                        expanded = yearExpanded,
                        onExpandedChange = { yearExpanded = !yearExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedYear.ifBlank { "選擇年級" },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = yearExpanded,
                            onDismissRequest = { yearExpanded = false }
                        ) {
                            years.forEach { year ->
                                DropdownMenuItem(
                                    text = { Text(year) },
                                    onClick = {
                                        selectedYear = year
                                        yearExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 班級下拉選單
                    ExposedDropdownMenuBox(
                        expanded = classExpanded,
                        onExpandedChange = { classExpanded = !classExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedClass.ifBlank { "選擇班級" },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = classExpanded,
                            onDismissRequest = { classExpanded = false }
                        ) {
                            classes.forEach { cls ->
                                DropdownMenuItem(
                                    text = { Text(cls) },
                                    onClick = {
                                        selectedClass = cls
                                        classExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("請選擇您的社團：", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))

                // 社團下拉選單
                ExposedDropdownMenuBox(
                    expanded = clubExpanded,
                    onExpandedChange = { clubExpanded = !clubExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedClub.ifBlank { "點擊選擇社團" },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clubExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = clubExpanded,
                        onDismissRequest = { clubExpanded = false }
                    ) {
                        clubs.forEach { club ->
                            DropdownMenuItem(
                                text = { Text(club) },
                                onClick = {
                                    selectedClub = club
                                    clubExpanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                // 老師介面保持手動輸入（因為老師可能要負責非標準命名的群組，或留給以後做擴充）
                OutlinedTextField(
                    value = teacherGroup,
                    onValueChange = { teacherGroup = it },
                    label = { Text("輸入負責班級或社團 (例: 一年一班)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (username.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "請輸入帳號和密碼", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val userDocRef = db.collection("users").document(username)

                if (isLogin) {
                    // 【登入邏輯】
                    userDocRef.get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            val dbPassword = document.getString("password") ?: ""
                            val dbIsStudent = document.getBoolean("isStudent") ?: true

                            if (dbIsStudent != isStudent) {
                                Toast.makeText(context, "登入身份不符", Toast.LENGTH_SHORT).show()
                            } else if (dbPassword == password) {
                                val savedRealName = document.getString("realName") ?: "未知使用者"
                                val savedClassName = document.getString("className") ?: ""
                                val savedClubName = document.getString("clubName") ?: ""
                                val savedTeacherGroup = document.getString("teacherGroup") ?: ""

                                Toast.makeText(context, "登入成功！", Toast.LENGTH_SHORT).show()
                                onSuccess(username, savedRealName, savedClassName, savedClubName, savedTeacherGroup)
                            } else {
                                Toast.makeText(context, "錯誤的密碼", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "未知的帳號", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // 【註冊邏輯】
                    if (realName.isBlank()) {
                        Toast.makeText(context, "請輸入真實姓名", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (isStudent && (selectedYear.isBlank() || selectedClass.isBlank() || selectedClub.isBlank())) {
                        Toast.makeText(context, "請選擇完整的班級與社團", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (!isStudent && teacherGroup.isBlank()) {
                        Toast.makeText(context, "請填寫負責群組", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // 💡 把選擇的「年級」與「班級」完美拼接在一起 (例如: "一年" + "一班" = "一年一班")
                    val finalClassName = "$selectedYear$selectedClass"

                    userDocRef.get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            Toast.makeText(context, "該帳號已被註冊過", Toast.LENGTH_SHORT).show()
                        } else {
                            val userData = hashMapOf(
                                "username" to username,
                                "password" to password,
                                "realName" to realName,
                                "isStudent" to isStudent,
                                "className" to if (isStudent) finalClassName else "",
                                "clubName" to if (isStudent) selectedClub else "",
                                "teacherGroup" to if (!isStudent) teacherGroup else ""
                            )

                            userDocRef.set(userData).addOnSuccessListener {
                                Toast.makeText(context, "註冊成功！", Toast.LENGTH_SHORT).show()
                                onSuccess(username, realName, finalClassName, selectedClub, teacherGroup)
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(actionName, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onSwitchMode) {
            Text(if (isLogin) "沒有帳號？點此註冊" else "已有帳號？點此登入")
        }
        TextButton(onClick = onBack) {
            Text("返回選擇身份")
        }
    }
}