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
import com.example.attendance.data.model.Department
import com.example.attendance.data.model.Employee
import com.example.attendance.ui.theme.*

@Composable
fun DepartmentsScreen(
    departments: List<Department>,
    employees: List<Employee>,
    onSaveDepartment: (Long?, String, String, (Boolean, String) -> Unit) -> Unit,
    onDeleteDepartment: (Department, (Boolean, String) -> Unit) -> Unit
) {
    var selectedDeptId by remember { mutableStateOf<Long?>(null) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var feedbackMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    fun clearForm() {
        selectedDeptId = null
        name = ""
        description = ""
        feedbackMessage = null
    }

    val empCounts = remember(departments, employees) {
        employees.groupBy { it.departmentId }.mapValues { it.value.size }
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
                    text = "إدارة الأقسام",
                    style = MaterialTheme.typography.headlineMedium,
                    color = NavyPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "إضافة وتعديل الأقسام ومتابعة عدد الموظفين في كل قسم",
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
                            text = if (selectedDeptId != null) "تعديل القسم" else "إضافة قسم جديد",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                        if (selectedDeptId != null) {
                            TextButton(onClick = { clearForm() }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("جديد")
                            }
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم القسم *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dept_name_input")
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("الوصف") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dept_desc_input")
                    )

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
                                if (name.isBlank()) {
                                    feedbackMessage = Pair(false, "اكتب اسم القسم أولاً.")
                                    return@Button
                                }
                                onSaveDepartment(selectedDeptId, name.trim(), description.trim()) { success, msg ->
                                    feedbackMessage = Pair(success, msg)
                                    if (success && selectedDeptId == null) clearForm()
                                }
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(46.dp)
                                .testTag("save_dept_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedDeptId != null) StatusOrange else StatusGreen
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (selectedDeptId != null) "تعديل القسم" else "حفظ القسم")
                        }

                        if (selectedDeptId != null) {
                            Button(
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("حذف")
                            }
                        }
                    }
                }
            }
        }

        // Header
        item {
            Text(
                text = "قائمة الأقسام (${departments.size})",
                style = MaterialTheme.typography.titleMedium,
                color = NavyPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        items(departments) { dept ->
            val isSelected = dept.id == selectedDeptId
            val count = empCounts[dept.id] ?: 0
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedDeptId = dept.id
                        name = dept.name
                        description = dept.description
                    }
                    .testTag("dept_card_${dept.id}"),
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
                        Text(
                            text = dept.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = NavyPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        if (dept.description.isNotBlank()) {
                            Text(
                                text = dept.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedText
                            )
                        }
                    }
                    Surface(
                        color = NavySecondary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "$count موظف",
                            color = NavyPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && selectedDeptId != null) {
        val dept = departments.firstOrNull { it.id == selectedDeptId }
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("تأكيد حذف القسم") },
            text = { Text("سيتم فصل الموظفين المرتبطين بالقسم (${dept?.name}) دون حذفهم. هل تريد المتابعة؟") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        dept?.let {
                            onDeleteDepartment(it) { success, msg ->
                                feedbackMessage = Pair(success, msg)
                                if (success) clearForm()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text("نعم، حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("إلغاء") }
            }
        )
    }
}
