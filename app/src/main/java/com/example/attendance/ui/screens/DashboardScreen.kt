package com.example.attendance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.attendance.data.repository.DashboardStats
import com.example.attendance.ui.theme.*
import com.example.attendance.ui.viewmodel.AppScreen

data class MetricCardData(
    val title: String,
    val value: String,
    val color: Color,
    val icon: ImageVector
)

@Composable
fun DashboardScreen(
    stats: DashboardStats,
    onNavigate: (AppScreen) -> Unit
) {
    val cards = listOf(
        MetricCardData("الموظفون النشطون", stats.totalEmployees.toString(), NavyPrimary, Icons.Default.People),
        MetricCardData("الحضور اليوم", stats.presentToday.toString(), StatusGreen, Icons.Default.CheckCircle),
        MetricCardData("الغياب اليوم", stats.absentToday.toString(), StatusRed, Icons.Default.Cancel),
        MetricCardData("انصراف غير مسجل", stats.openPunchCount.toString(), StatusOrange, Icons.Default.HourglassEmpty),
        MetricCardData("طلبات إجازة معلقة", stats.pendingLeavesCount.toString(), StatusRed, Icons.Default.DateRange),
        MetricCardData("الأقسام المسجلة", stats.departmentsCount.toString(), NavySecondary, Icons.Default.Business)
    )

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
                    text = "لوحة التحكم",
                    style = MaterialTheme.typography.headlineMedium,
                    color = NavyPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "نظرة عامة على حركة الموظفين ومؤشرات اليوم",
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

        // Metrics Grid (2 columns on mobile, adapts to 3 or 6 on wider displays)
        item {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val cols = if (maxWidth > 700.dp) 3 else 2
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val chunked = cards.chunked(cols)
                    chunked.forEach { rowCards ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowCards.forEach { card ->
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("metric_${card.title}"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = PaperSurface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(5.dp)
                                                .background(card.color)
                                        )
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = card.title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MutedText,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Icon(
                                                    card.icon,
                                                    contentDescription = null,
                                                    tint = card.color,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = card.value,
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NavyPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick shortcuts panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PaperSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "اختصارات سريعة",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onNavigate(AppScreen.PUNCH) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("shortcut_punch"),
                            colors = ButtonDefaults.buttonColors(containerColor = NavySecondary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تسجيل حركة", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { onNavigate(AppScreen.DAILY_LOG) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("shortcut_daily"),
                            colors = ButtonDefaults.buttonColors(containerColor = NavySecondary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("سجل اليوم", fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onNavigate(AppScreen.EMPLOYEES) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("shortcut_employees"),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إضافة موظف", fontSize = 12.sp, color = White)
                        }
                        Button(
                            onClick = { onNavigate(AppScreen.REPORTS) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("shortcut_reports"),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("التقرير الشهري", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Information banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PaperSurface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ملاحظة: يمكنك إدخال وقت الحضور والانصراف بصيغة 12 ساعة، ويحسب النظام ساعات العمل والتأخير والخروج المبكر والإضافي تلقائياً.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )
                }
            }
        }
    }
}
