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
import com.example.attendance.data.model.Shift
import com.example.attendance.ui.theme.*

@Composable
fun ShiftsScreen(
    shifts: List<Shift>,
    onSaveShift: (Long?, String, String, String, Int, (Boolean, String) -> Unit) -> Unit,
    onDeleteShift: (Shift, (Boolean, String) -> Unit) -> Unit
) {
    var selectedShiftId by remember { mutableStateOf<Long?>(null) }
    var name by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("08:00") }
    var endTime by remember { mutableStateOf("16:00") }
    var graceMinutes by remember { mutableStateOf("10") }
    var feedbackMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    fun clearForm() {
        selectedShiftId = null
        name = ""
        startTime = "08:00"
        endTime = "16:00"
        graceMinutes = "10"
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
                    text = "إدارة الورديات",
                    style = MaterialTheme.typography.headlineMedium,
                    color = NavyPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "تحديد مواعيد بدء ونهاية الدوام وفترة السماح لدقائق التأخير",
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
                            text = if (selectedShiftId != null) "تعديل الوردية" else "إضافة وردية جديدة",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                        if (selectedShiftId != null) {
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
                        label = { Text("اسم الوردية *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shift_name_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("بداية الدوام (HH:mm)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("نهاية الدوام (HH:mm)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = graceMinutes,
                        onValueChange = { graceMinutes = it },
                        label = { Text("دقائق سماح التأخير (افتراضي 10)") },
                        singleLine = true,
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
                                val grace = graceMinutes.toIntOrNull() ?: 10
                                if (name.isBlank()) {
                                    feedbackMessage = Pair(false, "اكتب اسم الوردية أولاً.")
                                    return@Button
                                }
                                onSaveShift(selectedShiftId, name.trim(), startTime.trim(), endTime.trim(), grace) { success, msg ->
                                    feedbackMessage = Pair(success, msg)
                                    if (success && selectedShiftId == null) clearForm()
                                }
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(46.dp)
                                .testTag("save_shift_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedShiftId != null) StatusOrange else StatusGreen
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (selectedShiftId != null) "تعديل الوردية" else "حفظ الوردية")
                        }

                        if (selectedShiftId != null) {
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
                text = "قائمة الورديات (${shifts.size})",
                style = MaterialTheme.typography.titleMedium,
                color = NavyPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        items(shifts) { shift ->
            val isSelected = shift.id == selectedShiftId
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedShiftId = shift.id
                        name = shift.name
                        startTime = shift.startTime
                        endTime = shift.endTime
                        graceMinutes = shift.graceMinutes.toString()
                    }
                    .testTag("shift_card_${shift.id}"),
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
                    Column {
                        Text(
                            text = shift.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = NavyPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "من ${shift.startTime} إلى ${shift.endTime} | سماح: ${shift.graceMinutes} دقيقة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedText
                        )
                    }
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = GoldAccent
                    )
                }
            }
        }
    }

    if (showDeleteConfirm && selectedShiftId != null) {
        val shift = shifts.firstOrNull { it.id == selectedShiftId }
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("تأكيد حذف الوردية") },
            text = { Text("حذف الوردية (${shift?.name})؟ لن يمكن الحذف إذا كانت مرتبطة بموظفين أو سجلات حضور.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        shift?.let {
                            onDeleteShift(it) { success, msg ->
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
