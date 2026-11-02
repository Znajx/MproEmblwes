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
import com.example.attendance.data.model.Employee
import com.example.attendance.data.model.LeaveRequest
import com.example.attendance.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeavesScreen(
    leaves: List<LeaveRequest>,
    employees: List<Employee>,
    onSaveLeave: (Long?, Long, String, String, String, String, String, (Boolean, String) -> Unit) -> Unit,
    onDeleteLeave: (LeaveRequest, (Boolean, String) -> Unit) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val today = remember { dateFormat.format(Date()) }

    var selectedLeaveId by remember { mutableStateOf<Long?>(null) }
    var selectedEmployeeId by remember { mutableStateOf<Long?>(employees.firstOrNull()?.id) }
    var startDate by remember { mutableStateOf(today) }
    var endDate by remember { mutableStateOf(today) }
    var leaveType by remember { mutableStateOf("سنوية") }
    var reason by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("قيد المراجعة") }

    var empMenuExpanded by remember { mutableStateOf(false) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var statusMenuExpanded by remember { mutableStateOf(false) }

    var feedbackMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val empMap = remember(employees) { employees.associateBy { it.id } }
    val leaveTypes = listOf("سنوية", "مرضية", "طارئة", "بدون راتب", "إذن")
    val statuses = listOf("قيد المراجعة", "موافق", "مرفوض")

    fun clearForm() {
        selectedLeaveId = null
        selectedEmployeeId = employees.firstOrNull()?.id
        startDate = today
        endDate = today
        leaveType = "سنوية"
        reason = ""
        status = "قيد المراجعة"
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
                    text = "إدارة الإجازات",
                    style = MaterialTheme.typography.headlineMedium,
                    color = NavyPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "تقديم ومراجعة طلبات الإجازات مع التحقق التلقائي من تداخل الفترات",
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
                            text = if (selectedLeaveId != null) "تعديل طلب الإجازة" else "طلب إجازة جديد",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                        if (selectedLeaveId != null) {
                            TextButton(onClick = { clearForm() }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("طلب جديد")
                            }
                        }
                    }

                    // Employee Dropdown
                    ExposedDropdownMenuBox(
                        expanded = empMenuExpanded,
                        onExpandedChange = { empMenuExpanded = !empMenuExpanded }
                    ) {
                        val currentEmpName = selectedEmployeeId?.let { empMap[it]?.fullName } ?: "اختر موظفاً"
                        OutlinedTextField(
                            value = currentEmpName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الموظف *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = empMenuExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth().testTag("leave_emp_select")
                        )
                        ExposedDropdownMenu(
                            expanded = empMenuExpanded,
                            onDismissRequest = { empMenuExpanded = false }
                        ) {
                            employees.forEach { emp ->
                                DropdownMenuItem(
                                    text = { Text("${emp.fullName} (${emp.empCode})") },
                                    onClick = {
                                        selectedEmployeeId = emp.id
                                        empMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Dates: From & To
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("من تاريخ (YYYY-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = { Text("إلى تاريخ (YYYY-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Type & Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = typeMenuExpanded,
                            onExpandedChange = { typeMenuExpanded = !typeMenuExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = leaveType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("نوع الإجازة") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = typeMenuExpanded,
                                onDismissRequest = { typeMenuExpanded = false }
                            ) {
                                leaveTypes.forEach { t ->
                                    DropdownMenuItem(text = { Text(t) }, onClick = { leaveType = t; typeMenuExpanded = false })
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = statusMenuExpanded,
                            onExpandedChange = { statusMenuExpanded = !statusMenuExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = status,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("حالة الطلب") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusMenuExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = statusMenuExpanded,
                                onDismissRequest = { statusMenuExpanded = false }
                            ) {
                                statuses.forEach { s ->
                                    DropdownMenuItem(text = { Text(s) }, onClick = { status = s; statusMenuExpanded = false })
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("سبب الإجازة") },
                        modifier = Modifier.fillMaxWidth()
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
                                val empId = selectedEmployeeId
                                if (empId == null) {
                                    feedbackMessage = Pair(false, "اختر موظفاً أولاً.")
                                    return@Button
                                }
                                onSaveLeave(selectedLeaveId, empId, startDate.trim(), endDate.trim(), leaveType, reason.trim(), status) { success, msg ->
                                    feedbackMessage = Pair(success, msg)
                                    if (success && selectedLeaveId == null) clearForm()
                                }
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(46.dp)
                                .testTag("save_leave_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedLeaveId != null) StatusOrange else StatusGreen
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (selectedLeaveId != null) "تعديل الطلب" else "حفظ الطلب")
                        }

                        if (selectedLeaveId != null) {
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

        // List Header
        item {
            Text(
                text = "سجل طلبات الإجازات (${leaves.size})",
                style = MaterialTheme.typography.titleMedium,
                color = NavyPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        items(leaves) { lv ->
            val isSelected = lv.id == selectedLeaveId
            val emp = empMap[lv.employeeId]
            val statusColor = when (lv.status) {
                "موافق" -> StatusGreen
                "مرفوض" -> StatusRed
                else -> StatusOrange
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedLeaveId = lv.id
                        selectedEmployeeId = lv.employeeId
                        startDate = lv.startDate
                        endDate = lv.endDate
                        leaveType = lv.leaveType
                        reason = lv.reason
                        status = lv.status
                    }
                    .testTag("leave_card_${lv.id}"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) GoldLight.copy(alpha = 0.25f) else PaperSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = emp?.fullName ?: "موظف #${lv.employeeId}",
                            style = MaterialTheme.typography.titleMedium,
                            color = NavyPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = lv.status,
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "نوع الإجازة: ${lv.leaveType} | من ${lv.startDate} إلى ${lv.endDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )
                    if (lv.reason.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "السبب: ${lv.reason}",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkText
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && selectedLeaveId != null) {
        val lv = leaves.firstOrNull { it.id == selectedLeaveId }
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("تأكيد حذف طلب الإجازة") },
            text = { Text("هل تريد حذف هذا الطلب بالتأكيد؟") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        lv?.let {
                            onDeleteLeave(it) { success, msg ->
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
