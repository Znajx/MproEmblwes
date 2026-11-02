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
import com.example.attendance.data.model.Shift
import com.example.attendance.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(
    employees: List<Employee>,
    departments: List<Department>,
    shifts: List<Shift>,
    onSaveEmployee: (Long?, String, String, String, Long?, Long?, String, String, (Boolean, String) -> Unit) -> Unit,
    onDeleteEmployee: (Employee, (Boolean, String) -> Unit) -> Unit
) {
    var selectedEmployeeId by remember { mutableStateOf<Long?>(null) }
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedDeptId by remember { mutableStateOf<Long?>(null) }
    var selectedShiftId by remember { mutableStateOf<Long?>(null) }
    var hireDate by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("نشط") }

    var searchQuery by remember { mutableStateOf("") }
    var deptFilterId by remember { mutableStateOf<Long?>(null) }

    var deptMenuExpanded by remember { mutableStateOf(false) }
    var shiftMenuExpanded by remember { mutableStateOf(false) }
    var statusMenuExpanded by remember { mutableStateOf(false) }
    var deptFilterExpanded by remember { mutableStateOf(false) }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    fun clearForm() {
        selectedEmployeeId = null
        code = ""
        name = ""
        phone = ""
        selectedDeptId = null
        selectedShiftId = null
        hireDate = ""
        status = "نشط"
        feedbackMessage = null
    }

    fun selectEmployee(emp: Employee) {
        selectedEmployeeId = emp.id
        code = emp.empCode
        name = emp.fullName
        phone = emp.phone
        selectedDeptId = emp.departmentId
        selectedShiftId = emp.shiftId
        hireDate = emp.hireDate
        status = emp.status
        feedbackMessage = null
    }

    val deptMap = remember(departments) { departments.associateBy { it.id } }
    val shiftMap = remember(shifts) { shifts.associateBy { it.id } }

    val filteredEmployees = employees.filter {
        (it.fullName.contains(searchQuery, ignoreCase = true) || it.empCode.contains(searchQuery, ignoreCase = true)) &&
        (deptFilterId == null || it.departmentId == deptFilterId)
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
                    text = "إدارة الموظفين",
                    style = MaterialTheme.typography.headlineMedium,
                    color = NavyPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "إضافة وتعديل وحذف بيانات الموظفين وتعيين الأقسام والورديات",
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
                            text = if (selectedEmployeeId != null) "تعديل الموظف المحدد" else "إضافة موظف جديد",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                        if (selectedEmployeeId != null) {
                            TextButton(onClick = { clearForm() }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إلغاء التحديد / جديد", fontSize = 12.sp)
                            }
                        }
                    }

                    // Code & Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            label = { Text("الرقم الوظيفي *") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("emp_code_input")
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("اسم الموظف *") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("emp_name_input")
                        )
                    }

                    // Phone & Hire Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("رقم الهاتف") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = hireDate,
                            onValueChange = { hireDate = it },
                            label = { Text("تاريخ التعيين (YYYY-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Department & Shift Dropdowns
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Dept Dropdown
                        ExposedDropdownMenuBox(
                            expanded = deptMenuExpanded,
                            onExpandedChange = { deptMenuExpanded = !deptMenuExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedDeptId?.let { deptMap[it]?.name } ?: "اختر القسم",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("القسم") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptMenuExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = deptMenuExpanded,
                                onDismissRequest = { deptMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("بدون قسم") },
                                    onClick = {
                                        selectedDeptId = null
                                        deptMenuExpanded = false
                                    }
                                )
                                departments.forEach { d ->
                                    DropdownMenuItem(
                                        text = { Text(d.name) },
                                        onClick = {
                                            selectedDeptId = d.id
                                            deptMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Shift Dropdown
                        ExposedDropdownMenuBox(
                            expanded = shiftMenuExpanded,
                            onExpandedChange = { shiftMenuExpanded = !shiftMenuExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedShiftId?.let { shiftMap[it]?.name } ?: "اختر الوردية",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("الوردية") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = shiftMenuExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = shiftMenuExpanded,
                                onDismissRequest = { shiftMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("بدون وردية") },
                                    onClick = {
                                        selectedShiftId = null
                                        shiftMenuExpanded = false
                                    }
                                )
                                shifts.forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s.name) },
                                        onClick = {
                                            selectedShiftId = s.id
                                            shiftMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Status Dropdown
                    ExposedDropdownMenuBox(
                        expanded = statusMenuExpanded,
                        onExpandedChange = { statusMenuExpanded = !statusMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = status,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("حالة الموظف") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusMenuExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = statusMenuExpanded,
                            onDismissRequest = { statusMenuExpanded = false }
                        ) {
                            DropdownMenuItem(text = { Text("نشط") }, onClick = { status = "نشط"; statusMenuExpanded = false })
                            DropdownMenuItem(text = { Text("غير نشط") }, onClick = { status = "غير نشط"; statusMenuExpanded = false })
                        }
                    }

                    // Feedback Message
                    feedbackMessage?.let { (success, msg) ->
                        Text(
                            text = msg,
                            color = if (success) StatusGreen else StatusRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (code.isBlank() || name.isBlank()) {
                                    feedbackMessage = Pair(false, "الرقم الوظيفي واسم الموظف مطلوبان.")
                                    return@Button
                                }
                                onSaveEmployee(selectedEmployeeId, code.trim(), name.trim(), phone.trim(), selectedDeptId, selectedShiftId, hireDate.trim(), status) { success, msg ->
                                    feedbackMessage = Pair(success, msg)
                                    if (success && selectedEmployeeId == null) {
                                        clearForm()
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(46.dp)
                                .testTag("save_employee_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedEmployeeId != null) StatusOrange else StatusGreen
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(if (selectedEmployeeId != null) Icons.Default.Edit else Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (selectedEmployeeId != null) "تعديل الموظف المحدد" else "حفظ موظف جديد")
                        }

                        if (selectedEmployeeId != null) {
                            Button(
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("delete_employee_button"),
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

        // Search & Filter Toolbar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PaperSurface)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("بحث بالاسم أو الرقم...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MutedText) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        // Dept filter
                        ExposedDropdownMenuBox(
                            expanded = deptFilterExpanded,
                            onExpandedChange = { deptFilterExpanded = !deptFilterExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = deptFilterId?.let { deptMap[it]?.name } ?: "كل الأقسام",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptFilterExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = deptFilterExpanded,
                                onDismissRequest = { deptFilterExpanded = false }
                            ) {
                                DropdownMenuItem(text = { Text("كل الأقسام") }, onClick = { deptFilterId = null; deptFilterExpanded = false })
                                departments.forEach { d ->
                                    DropdownMenuItem(text = { Text(d.name) }, onClick = { deptFilterId = d.id; deptFilterExpanded = false })
                                }
                            }
                        }
                    }
                }
            }
        }

        // List Header
        item {
            Text(
                text = "قائمة الموظفين (${filteredEmployees.size})",
                style = MaterialTheme.typography.titleMedium,
                color = NavyPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // Employees List
        items(filteredEmployees) { emp ->
            val isSelected = emp.id == selectedEmployeeId
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectEmployee(emp) }
                    .testTag("emp_card_${emp.id}"),
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
                                text = emp.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                color = NavyPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = if (emp.status == "نشط") StatusGreen.copy(alpha = 0.15f) else StatusRed.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = emp.status,
                                    color = if (emp.status == "نشط") StatusGreen else StatusRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "الرقم: ${emp.empCode} | القسم: ${emp.departmentId?.let { deptMap[it]?.name } ?: "—"} | الوردية: ${emp.shiftId?.let { shiftMap[it]?.name } ?: "—"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedText
                        )
                        if (emp.phone.isNotBlank()) {
                            Text(
                                text = "الهاتف: ${emp.phone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedText
                            )
                        }
                    }

                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "تحديد",
                        tint = if (isSelected) GoldAccent else MutedText
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm && selectedEmployeeId != null) {
        val emp = employees.firstOrNull { it.id == selectedEmployeeId }
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("تأكيد الحذف النهائي", fontWeight = FontWeight.Bold, color = StatusRed) },
            text = {
                Text(
                    "هل أنت متأكد من حذف الموظف ${emp?.fullName} (${emp?.empCode}) نهائياً؟\nسيتم حذف جميع سجلات حضوره وطلبات إجازاته المرتبطة به. لا يمكن التراجع عن هذا الإجراء."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        emp?.let {
                            onDeleteEmployee(it) { success, msg ->
                                feedbackMessage = Pair(success, msg)
                                if (success) clearForm()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text("نعم، حذف نهائي")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
