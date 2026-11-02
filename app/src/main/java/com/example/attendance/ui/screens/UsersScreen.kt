package com.example.attendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.attendance.data.model.User
import com.example.attendance.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    users: List<User>,
    currentLoggedInUser: User?,
    onSaveUser: (Long?, String, String, String, Boolean, String, (Boolean, String) -> Unit) -> Unit,
    onDeactivateUser: (User, (Boolean, String) -> Unit) -> Unit
) {
    var selectedUserId by remember { mutableStateOf<Long?>(null) }
    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("مستخدم") }
    var active by remember { mutableStateOf(true) }

    var roleMenuExpanded by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    val roles = listOf("مدير", "مسؤول", "مستخدم")

    fun clearForm() {
        selectedUserId = null
        username = ""
        fullName = ""
        password = ""
        role = "مستخدم"
        active = true
        feedbackMessage = null
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "المستخدمون والصلاحيات",
                    style = MaterialTheme.typography.headlineMedium,
                    color = NavyPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "إدارة حسابات النظام وتعيين الأدوار (مدير / مسؤول / مستخدم) وتفعيل أو تعطيل الحسابات",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedText
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(3.dp)
                        .background(GoldAccent, RoundedCornerShape(2.dp))
                )
            }
        }

        // Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PaperSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedUserId != null) "تعديل بيانات الحساب" else "إنشاء حساب مستخدم جديد",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                        if (selectedUserId != null) {
                            TextButton(onClick = { clearForm() }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("حساب جديد")
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("اسم المستخدم *") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("username_field")
                        )
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("الاسم الكامل *") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(if (selectedUserId != null) "كلمة المرور (اتركها فارغة للإبقاء)" else "كلمة المرور (8 أحرف فأكثر) *") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        ExposedDropdownMenuBox(
                            expanded = roleMenuExpanded,
                            onExpandedChange = { roleMenuExpanded = !roleMenuExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = role,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("الدور") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleMenuExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = roleMenuExpanded,
                                onDismissRequest = { roleMenuExpanded = false }
                            ) {
                                roles.forEach { r ->
                                    DropdownMenuItem(text = { Text(r) }, onClick = { role = r; roleMenuExpanded = false })
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = active,
                            onCheckedChange = { active = it },
                            colors = CheckboxDefaults.colors(checkedColor = StatusGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("الحساب نشط ومفعّل", color = NavyPrimary, fontSize = 14.sp)
                    }

                    feedbackMessage?.let { (success, msg) ->
                        Text(
                            text = msg,
                            color = if (success) StatusGreen else StatusRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (username.isBlank() || fullName.isBlank()) {
                                    feedbackMessage = Pair(false, "اسم المستخدم والاسم الكامل مطلوبان.")
                                    return@Button
                                }
                                onSaveUser(selectedUserId, username.trim(), fullName.trim(), role, active, password) { success, msg ->
                                    feedbackMessage = Pair(success, msg)
                                    if (success && selectedUserId == null) clearForm()
                                }
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(46.dp)
                                .testTag("save_user_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedUserId != null) StatusOrange else StatusGreen
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (selectedUserId != null) "تعديل الحساب" else "حفظ الحساب")
                        }

                        if (selectedUserId != null && selectedUserId != currentLoggedInUser?.id) {
                            Button(
                                onClick = {
                                    val target = users.firstOrNull { it.id == selectedUserId }
                                    target?.let {
                                        onDeactivateUser(it) { success, msg ->
                                            feedbackMessage = Pair(success, msg)
                                            if (success) clearForm()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تعطيل")
                            }
                        }
                    }
                }
            }
        }

        // Users List Header
        item {
            Text(
                text = "قائمة المستخدمين (${users.size})",
                style = MaterialTheme.typography.titleMedium,
                color = NavyPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        items(users) { u ->
            val isSelected = u.id == selectedUserId
            val isCurrent = u.id == currentLoggedInUser?.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedUserId = u.id
                        username = u.username
                        fullName = u.fullName
                        role = u.role
                        active = u.active
                        password = ""
                    }
                    .testTag("user_card_${u.id}"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) GoldLight.copy(alpha = 0.25f) else PaperSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = u.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                color = NavyPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            if (isCurrent) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = GoldAccent.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "حسابك الحالي",
                                        color = GoldAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "اسم المستخدم: ${u.username} | الدور: ${u.role}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedText
                        )
                    }

                    Surface(
                        color = if (u.active) StatusGreen.copy(alpha = 0.15f) else StatusRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (u.active) "نشط" else "معطل",
                            color = if (u.active) StatusGreen else StatusRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}
