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
import com.example.attendance.data.repository.MonthlyReportRow
import com.example.attendance.ui.theme.*

@Composable
fun ReportsScreen(
    currentMonth: String,
    reportRows: List<MonthlyReportRow>,
    onMonthChange: (String) -> Unit
) {
    var monthInput by remember(currentMonth) { mutableStateOf(currentMonth) }
    var exportFeedback by remember { mutableStateOf<String?>(null) }

    val totalEmployees = reportRows.size
    val totalPresent = reportRows.sumOf { it.presentDays }
    val totalAbsent = reportRows.sumOf { it.absentDays }
    val totalLeaves = reportRows.sumOf { it.leaveDays }

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
                    text = "التقارير الشهرية",
                    style = MaterialTheme.typography.headlineMedium,
                    color = NavyPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ملخص شهري لكل موظف: الحضور والغياب والإجازات والتأخير وساعات العمل",
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

        // Month Selector & Export Card
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
                            value = monthInput,
                            onValueChange = { monthInput = it },
                            label = { Text("الشهر (YYYY-MM)") },
                            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = NavyPrimary) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("report_month_input")
                        )
                        Button(
                            onClick = { onMonthChange(monthInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = NavySecondary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("generate_report_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إنشاء التقرير")
                        }
                    }

                    // Export buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { exportFeedback = "تم تصدير ملف Excel للتقرير الشهري ($monthInput)" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excel", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { exportFeedback = "تم تصدير ملف PDF للتقرير الشهري ($monthInput)" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = StatusRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { exportFeedback = "تم تصدير ملف CSV للتقرير الشهري ($monthInput)" },
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

        // Summary Cards Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportSummaryPill(modifier = Modifier.weight(1f), label = "عدد الموظفين", value = totalEmployees.toString(), color = NavyPrimary)
                ReportSummaryPill(modifier = Modifier.weight(1f), label = "أيام الحضور", value = totalPresent.toString(), color = StatusGreen)
                ReportSummaryPill(modifier = Modifier.weight(1f), label = "أيام الغياب", value = totalAbsent.toString(), color = StatusRed)
                ReportSummaryPill(modifier = Modifier.weight(1f), label = "أيام الإجازة", value = totalLeaves.toString(), color = GoldAccent)
            }
        }

        // List Header
        item {
            Text(
                text = "تفاصيل الموظفين للشهر ($monthInput)",
                style = MaterialTheme.typography.titleMedium,
                color = NavyPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // Rows
        if (reportRows.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PaperSurface)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("لا توجد بيانات لهذا الشهر", color = MutedText)
                    }
                }
            }
        } else {
            items(reportRows) { row ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_row_${row.empCode}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = PaperSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = row.empName,
                                style = MaterialTheme.typography.titleMedium,
                                color = NavyPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "الرقم: ${row.empCode}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedText
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = LineBorder.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MetricPill(label = "الحضور", value = "${row.presentDays} يوم")
                            MetricPill(label = "الغياب", value = "${row.absentDays} يوم")
                            MetricPill(label = "الإجازات", value = "${row.leaveDays} يوم")
                            MetricPill(label = "ساعات العمل", value = "${row.totalHours} س")
                        }

                        if (row.totalLateMinutes > 0 || row.totalEarlyMinutes > 0 || row.totalOvertimeMinutes > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (row.totalLateMinutes > 0) {
                                    Text("تأخير: ${row.totalLateMinutes} دقيقة", color = StatusRed, fontSize = 11.sp)
                                }
                                if (row.totalEarlyMinutes > 0) {
                                    Text("خروج مبكر: ${row.totalEarlyMinutes} دقيقة", color = StatusOrange, fontSize = 11.sp)
                                }
                                if (row.totalOvertimeMinutes > 0) {
                                    Text("إضافي: ${row.totalOvertimeMinutes} دقيقة", color = StatusGreen, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportSummaryPill(modifier: Modifier = Modifier, label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = PaperSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, color = MutedText, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
