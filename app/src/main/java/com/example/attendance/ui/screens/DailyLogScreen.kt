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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.attendance.data.repository.AttendanceRowItem
import com.example.attendance.ui.theme.*

@Composable
fun DailyLogScreen(
    currentDate: String,
    rows: List<AttendanceRowItem>,
    onDateChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onEditRecord: (AttendanceRowItem, String, String, String) -> Unit
) {
    var dateInput by remember(currentDate) { mutableStateOf(currentDate) }
    var selectedRowForEdit by remember { mutableStateOf<AttendanceRowItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var exportFeedback by remember { mutableStateOf<String?>(null) }

    val filteredRows = rows.filter {
        it.empName.contains(searchQuery, ignoreCase = true) ||
        it.empCode.contains(searchQuery, ignoreCase = true) ||
        it.status.contains(searchQuery, ignoreCase = true)
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
                    text = "سجل الحضور اليومي",
                    style = MaterialTheme.typography.headlineMedium,
                    color = NavyPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "عرض تفاصيل حضور وانصراف الموظفين وساعات العمل والتأخير",
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

        // Filter and Date Toolbar Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PaperSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = dateInput,
                            onValueChange = { dateInput = it },
                            label = { Text("التاريخ (YYYY-MM-DD)") },
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = NavyPrimary) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("log_date_field")
                        )
                        Button(
                            onClick = { onDateChange(dateInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = NavySecondary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("apply_date_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("عرض")
                        }
                    }

                    // Search within daily log
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("بحث بالاسم أو الرقم أو الحالة...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MutedText) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Export actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { exportFeedback = "تم تجهيز ملف Excel لسجل الحضور ($dateInput)" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excel", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { exportFeedback = "تم تجهيز ملف PDF لسجل الحضور ($dateInput)" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = StatusRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { exportFeedback = "تم تجهيز ملف CSV لسجل الحضور ($dateInput)" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CSV", fontSize = 12.sp)
                        }
                    }

                    exportFeedback?.let { msg ->
                        Text(
                            text = msg,
                            color = StatusGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Summary row badge
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "عدد الموظفين: ${filteredRows.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = NavyPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "انقر على السجل لتعديله",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )
            }
        }

        // List of Attendance Rows
        if (filteredRows.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PaperSurface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("لا توجد سجلات مطابقة لهذا اليوم", color = MutedText)
                    }
                }
            }
        } else {
            items(filteredRows) { row ->
                AttendanceCardItem(
                    row = row,
                    onEditClick = { selectedRowForEdit = row }
                )
            }
        }
    }

    // Edit Attendance Dialog
    selectedRowForEdit?.let { row ->
        EditAttendanceDialog(
            row = row,
            date = dateInput,
            onDismiss = { selectedRowForEdit = null },
            onSave = { checkIn, checkOut, notes ->
                onEditRecord(row, checkIn, checkOut, notes)
                selectedRowForEdit = null
            }
        )
    }
}

@Composable
fun AttendanceCardItem(
    row: AttendanceRowItem,
    onEditClick: () -> Unit
) {
    val statusColor = when (row.status) {
        "حاضر" -> StatusGreen
        "إجازة" -> GoldAccent
        "لم يسجل الانصراف" -> StatusOrange
        "غياب" -> StatusRed
        else -> MutedText
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() }
            .testTag("attendance_row_${row.employeeId}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PaperSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Name & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = row.empName,
                        style = MaterialTheme.typography.titleMedium,
                        color = NavyPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "الرقم: ${row.empCode} | الوردية: ${row.shiftName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )
                }
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = row.status,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = LineBorder.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(10.dp))

            // Timings & Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricPill(label = "الحضور", value = row.checkIn12)
                MetricPill(label = "الانصراف", value = row.checkOut12)
                MetricPill(label = "الساعات", value = if (row.totalHours != "—") "${row.totalHours} س" else "—")
                MetricPill(label = "التأخير", value = if (row.lateMinutes != "—") "${row.lateMinutes} د" else "—")
            }

            if (row.earlyMinutes != "—" || row.overtimeMinutes != "—") {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (row.earlyMinutes != "—") {
                        Text(
                            text = "خروج مبكر: ${row.earlyMinutes} دقيقة",
                            color = StatusOrange,
                            fontSize = 12.sp
                        )
                    }
                    if (row.overtimeMinutes != "—") {
                        Text(
                            text = "إضافي: ${row.overtimeMinutes} دقيقة",
                            color = StatusGreen,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MutedText, fontSize = 11.sp)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = NavyPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EditAttendanceDialog(
    row: AttendanceRowItem,
    date: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var checkIn by remember { mutableStateOf(if (row.checkIn12 != "—") row.checkIn12 else "") }
    var checkOut by remember { mutableStateOf(if (row.checkOut12 != "—") row.checkOut12 else "") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("تعديل سجل: ${row.empName}", fontWeight = FontWeight.Bold, color = NavyPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("التاريخ: $date | الوردية: ${row.shiftName}", color = MutedText, fontSize = 13.sp)
                OutlinedTextField(
                    value = checkIn,
                    onValueChange = { checkIn = it },
                    label = { Text("وقت الحضور (مثال: 08:30 AM)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = checkOut,
                    onValueChange = { checkOut = it },
                    label = { Text("وقت الانصراف (مثال: 04:30 PM)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(checkIn, checkOut, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
            ) {
                Text("حفظ التعديل")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = MutedText)
            }
        }
    )
}
