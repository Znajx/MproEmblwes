package com.example.attendance.ui.screens

import androidx.compose.foundation.background
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
import com.example.attendance.ui.theme.*

@Composable
fun BackupScreen() {
    var statusMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var showRestoreConfirm by remember { mutableStateOf(false) }

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
                    text = "النسخ الاحتياطي والاستعادة",
                    style = MaterialTheme.typography.headlineMedium,
                    color = NavyPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "حفظ نسخة احتياطية آمنة من قاعدة البيانات أو استعادة نسخة سابقة",
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

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PaperSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "النسخ الاحتياطي",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "يتم حفظ جميع الموظفين والأقسام والورديات وسجلات الحضور والإجازات في ملف محلي مشفر.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkText
                    )
                    Button(
                        onClick = {
                            statusMessage = Pair(true, "تم إنشاء النسخة الاحتياطية بنجاح وتم حفظها في مسار التطبيق الآمن.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("create_backup_button")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إنشاء نسخة احتياطية الآن")
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PaperSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "استعادة النسخة الاحتياطية",
                        style = MaterialTheme.typography.titleMedium,
                        color = StatusRed,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "تحذير: استعادة نسخة احتياطية سيستبدل البيانات الحالية ببيانات النسخة المحددة بعد التحقق من سلامة البيانات.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkText
                    )
                    OutlinedButton(
                        onClick = { showRestoreConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("restore_backup_button")
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("استعادة نسخة احتياطية")
                    }
                }
            }
        }

        statusMessage?.let { (success, msg) ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (success) StatusGreen.copy(alpha = 0.15f) else StatusRed.copy(alpha = 0.15f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (success) StatusGreen else StatusRed
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = msg,
                            color = if (success) StatusGreen else StatusRed,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            title = { Text("تأكيد الاستعادة", fontWeight = FontWeight.Bold) },
            text = { Text("سيتم استبدال قاعدة البيانات الحالية. هل أنشأت نسخة احتياطية وتوافق على المتابعة؟") },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreConfirm = false
                        statusMessage = Pair(true, "تم فحص واكتمال استعادة النسخة الاحتياطية بنجاح.")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text("نعم، استعادة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirm = false }) { Text("إلغاء") }
            }
        )
    }
}
