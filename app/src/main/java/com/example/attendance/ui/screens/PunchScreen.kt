package com.example.attendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.attendance.data.model.Shift
import com.example.attendance.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PunchScreen(
    employees: List<Employee>,
    shifts: List<Shift>,
    onPunch: (Long, String, String, Long?, Boolean, (Boolean, String) -> Unit) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val time12Format = remember { SimpleDateFormat("hh:mm a", Locale.US) }
    val time24Format = remember { SimpleDateFormat("HH:mm", Locale.US) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedEmployee by remember { mutableStateOf<Employee?>(null) }
    var workDate by remember { mutableStateOf(dateFormat.format(Date())) }
    var timeString by remember { mutableStateOf(time12Format.format(Date())) }
    var selectedShiftId by remember { mutableStateOf<Long?>(shifts.firstOrNull()?.id) }

    var shiftDropdownExpanded by remember { mutableStateOf(false) }
    var employeeDropdownExpanded by remember { mutableStateOf(false) }

    var feedbackMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    // When employee is selected, default to employee's assigned shift if available
    LaunchedEffect(selectedEmployee) {
        selectedEmployee?.shiftId?.let { sId ->
            selectedShiftId = sId
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "تسجيل الحضور والانصراف",
                    style = MaterialTheme.typography.headlineMedium,
                    color = NavyPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ابحث بالاسم أو الرقم الوظيفي، وحدد التاريخ والوقت والوردية",
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

        // Main Punch Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PaperSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "بيانات الحركة",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )

                    // Employee Search & Select
                    val filteredEmployees = employees.filter {
                        it.fullName.contains(searchQuery, ignoreCase = true) ||
                        it.empCode.contains(searchQuery, ignoreCase = true)
                    }

                    Column {
                        OutlinedTextField(
                            value = if (selectedEmployee != null) "${selectedEmployee?.fullName} (${selectedEmployee?.empCode})" else searchQuery,
                            onValueChange = {
                                searchQuery = it
                                selectedEmployee = null
                                employeeDropdownExpanded = it.isNotBlank()
                            },
                            label = { Text("الموظف (ابحث بالاسم أو الرقم الوظيفي)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NavyPrimary) },
                            trailingIcon = {
                                if (selectedEmployee != null) {
                                    IconButton(onClick = {
                                        selectedEmployee = null
                                        searchQuery = ""
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = "مسح الاختيار")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("punch_employee_input")
                        )

                        if (employeeDropdownExpanded && selectedEmployee == null && filteredEmployees.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = PaperSurface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                LazyColumn {
                                    items(filteredEmployees.size) { idx ->
                                        val emp = filteredEmployees[idx]
                                        ListItem(
                                            headlineContent = { Text(emp.fullName, fontWeight = FontWeight.Bold) },
                                            supportingContent = { Text("الرقم: ${emp.empCode} | الحالة: ${emp.status}") },
                                            modifier = Modifier
                                                .clickable {
                                                    selectedEmployee = emp
                                                    searchQuery = emp.fullName
                                                    employeeDropdownExpanded = false
                                                }
                                                .testTag("employee_select_${emp.id}")
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Date & Time Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = workDate,
                            onValueChange = { workDate = it },
                            label = { Text("التاريخ (YYYY-MM-DD)") },
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = NavyPrimary) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("punch_date_input")
                        )

                        OutlinedTextField(
                            value = timeString,
                            onValueChange = { timeString = it },
                            label = { Text("الوقت (مثال: 08:30 AM)") },
                            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = NavyPrimary) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("punch_time_input")
                        )
                    }

                    // Shift Selection Dropdown
                    ExposedDropdownMenuBox(
                        expanded = shiftDropdownExpanded,
                        onExpandedChange = { shiftDropdownExpanded = !shiftDropdownExpanded }
                    ) {
                        val currentShiftName = shifts.firstOrNull { it.id == selectedShiftId }?.name ?: "اختر الوردية"
                        OutlinedTextField(
                            value = currentShiftName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الوردية") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = shiftDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("shift_selector")
                        )
                        ExposedDropdownMenu(
                            expanded = shiftDropdownExpanded,
                            onDismissRequest = { shiftDropdownExpanded = false }
                        ) {
                            shifts.forEach { shift ->
                                DropdownMenuItem(
                                    text = { Text("${shift.name} (${shift.startTime} - ${shift.endTime})") },
                                    onClick = {
                                        selectedShiftId = shift.id
                                        shiftDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Employee status summary pill
                    if (selectedEmployee != null) {
                        val emp = selectedEmployee!!
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CreamBackground),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = GoldAccent)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${emp.fullName} — الرقم الوظيفي: ${emp.empCode}",
                                        fontWeight = FontWeight.Bold,
                                        color = NavyPrimary,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "الحالة: ${emp.status}",
                                        color = if (emp.status == "نشط") StatusGreen else StatusRed,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // Feedback Message if any
                    feedbackMessage?.let { (success, msg) ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (success) StatusGreen.copy(alpha = 0.15f) else StatusRed.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (success) StatusGreen else StatusRed
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg,
                                    color = if (success) StatusGreen else StatusRed,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Helper to convert timeString to HH:mm
                    fun parseTo24(timeStr: String): String {
                        val clean = timeStr.trim().uppercase()
                            .replace("ص", "AM")
                            .replace("م", "PM")
                            .replace(" ", "")
                        for (pattern in listOf("hh:mma", "ha", "HH:mm", "h:mma")) {
                            try {
                                val d = SimpleDateFormat(pattern, Locale.US).parse(clean)
                                if (d != null) return SimpleDateFormat("HH:mm", Locale.US).format(d)
                            } catch (_: Exception) {}
                        }
                        return "08:00"
                    }

                    // Action Buttons: Punch In & Punch Out
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val emp = selectedEmployee
                                if (emp == null) {
                                    feedbackMessage = Pair(false, "اختر موظفاً أولاً.")
                                    return@Button
                                }
                                val t24 = parseTo24(timeString)
                                onPunch(emp.id, workDate, t24, selectedShiftId, true) { success, msg ->
                                    feedbackMessage = Pair(success, msg)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("punch_in_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تسجيل حضور", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val emp = selectedEmployee
                                if (emp == null) {
                                    feedbackMessage = Pair(false, "اختر موظفاً أولاً.")
                                    return@Button
                                }
                                val t24 = parseTo24(timeString)
                                onPunch(emp.id, workDate, t24, selectedShiftId, false) { success, msg ->
                                    feedbackMessage = Pair(success, msg)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("punch_out_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusOrange),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تسجيل انصراف", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
