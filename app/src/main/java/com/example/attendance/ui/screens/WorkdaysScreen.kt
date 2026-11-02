package com.example.attendance.ui.screens

import androidx.compose.foundation.background
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
import com.example.attendance.data.model.Holiday
import com.example.attendance.data.model.WorkDay
import com.example.attendance.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WorkdaysScreen(
    workdays: List<WorkDay>,
    holidays: List<Holiday>,
    onSaveWorkdays: (List<WorkDay>, (Boolean, String) -> Unit) -> Unit,
    onAddHoliday: (String, String, String, (Boolean, String) -> Unit) -> Unit,
    onDeleteHoliday: (Holiday, (Boolean, String) -> Unit) -> Unit
) {
    val dayNames = listOf("الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت", "الأحد")

    val workdayStates = remember(workdays) {
        val map = workdays.associate { it.weekday to it.isWorkday }
        (0..6).map { idx ->
            mutableStateOf(map[idx] ?: (idx != 4 && idx != 5))
        }
    }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    var holidayDate by remember { mutableStateOf(dateFormat.format(Date())) }
    var holidayName by remember { mutableStateOf("") }
    var holidayNotes by remember { mutableStateOf("") }
    var feedbackMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

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
                    text = "أيام العمل والعطلات الرسمية",
                    style = MaterialTheme.typography.headlineMedium,
                    color = NavyPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "حدد أيام العمل الأسبوعية وأضف العطلات الرسمية لاستثنائها من احتساب الغياب",
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

        // Weekly Workdays Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PaperSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "أيام العمل الأسبوعية",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        dayNames.forEachIndexed { index, name ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(name, style = MaterialTheme.typography.bodyMedium, color = NavyPrimary)
                                Checkbox(
                                    checked = workdayStates[index].value,
                                    onCheckedChange = { workdayStates[index].value = it },
                                    colors = CheckboxDefaults.colors(checkedColor = StatusGreen)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val updated = (0..6).map { idx ->
                                WorkDay(weekday = idx, isWorkday = workdayStates[idx].value)
                            }
                            onSaveWorkdays(updated) { success, msg ->
                                feedbackMessage = Pair(success, msg)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("save_workdays_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ أيام العمل")
                    }
                }
            }
        }

        // Add Holiday Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PaperSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "إضافة عطلة رسمية",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = holidayDate,
                            onValueChange = { holidayDate = it },
                            label = { Text("التاريخ (YYYY-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = holidayName,
                            onValueChange = { holidayName = it },
                            label = { Text("اسم المناسبة *") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = holidayNotes,
                        onValueChange = { holidayNotes = it },
                        label = { Text("ملاحظات") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (holidayName.isBlank()) {
                                feedbackMessage = Pair(false, "اكتب اسم المناسبة أولاً.")
                                return@Button
                            }
                            onAddHoliday(holidayDate.trim(), holidayName.trim(), holidayNotes.trim()) { success, msg ->
                                feedbackMessage = Pair(success, msg)
                                if (success) {
                                    holidayName = ""
                                    holidayNotes = ""
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("add_holiday_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NavySecondary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة عطلة")
                    }
                }
            }
        }

        feedbackMessage?.let { (success, msg) ->
            item {
                Text(
                    text = msg,
                    color = if (success) StatusGreen else StatusRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Holidays List Header
        item {
            Text(
                text = "قائمة العطلات الرسمية (${holidays.size})",
                style = MaterialTheme.typography.titleMedium,
                color = NavyPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        items(holidays) { h ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = PaperSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                            text = h.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = NavyPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "التاريخ: ${h.holidayDate}${if (h.notes.isNotBlank()) " | ${h.notes}" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedText
                        )
                    }
                    IconButton(onClick = { onDeleteHoliday(h) { _, _ -> } }) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف العطلة", tint = StatusRed)
                    }
                }
            }
        }
    }
}
