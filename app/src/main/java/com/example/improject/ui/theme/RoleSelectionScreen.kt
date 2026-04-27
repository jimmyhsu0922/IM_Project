package com.example.improject.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RoleSelectionScreen(
    onRoleSelected: (Boolean) -> Unit // 傳回 true 代表學生，false 代表老師
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "請選擇你的身分",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // 學生按鈕
        Button(
            onClick = { onRoleSelected(true) },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(80.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)) // 活潑的綠色
        ) {
            Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text("我是學生", fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 老師按鈕
        Button(
            onClick = { onRoleSelected(false) },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(80.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6)) // 沉穩的藍色
        ) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text("我是老師", fontSize = 24.sp)
        }
    }
}