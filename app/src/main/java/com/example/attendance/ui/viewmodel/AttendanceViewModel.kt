package com.example.attendance.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.attendance.data.db.AppDatabase
import com.example.attendance.data.model.*
import com.example.attendance.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class AppScreen(val title: String, val iconName: String, val allowedRoles: List<String>) {
    DASHBOARD("لوحة التحكم", "Dashboard", listOf("مدير", "مسؤول", "مستخدم")),
    PUNCH("تسجيل الحركة", "Punch", listOf("مدير", "مسؤول", "مستخدم")),
    DAILY_LOG("سجل الحضور اليومي", "Daily", listOf("مدير", "مسؤول", "مستخدم")),
    EMPLOYEES("إدارة الموظفين", "Employees", listOf("مدير", "مسؤول")),
    DEPARTMENTS("إدارة الأقسام", "Departments", listOf("مدير", "مسؤول")),
    SHIFTS("إدارة الورديات", "Shifts", listOf("مدير", "مسؤول")),
    LEAVES("إدارة الإجازات", "Leaves", listOf("مدير", "مسؤول")),
    REPORTS("التقارير الشهرية", "Reports", listOf("مدير", "مسؤول")),
    CALENDAR("أيام العمل والعطلات", "Calendar", listOf("مدير")),
    USERS("المستخدمون والصلاحيات", "Users", listOf("مدير")),
    AUDIT("سجل العمليات", "Audit", listOf("مدير")),
    BACKUP("النسخ الاحتياطي", "Backup", listOf("مدير"))
}

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    val repository = AttendanceRepository(db)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val time24Format = SimpleDateFormat("HH:mm", Locale.US)
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.US)

    // Current logged in user
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Navigation
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Message / Toast banner
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    // Data Flows
    val departments = repository.departmentsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val shifts = repository.shiftsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val employees = repository.employeesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val leaves = repository.leavesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val workdays = repository.workdaysFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val holidays = repository.holidaysFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val users = repository.usersFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val auditLogs = repository.auditLogsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard State
    private val _dashboardStats = MutableStateFlow(DashboardStats(0, 0, 0, 0, 0, 0))
    val dashboardStats: StateFlow<DashboardStats> = _dashboardStats.asStateFlow()

    // Daily Log State
    private val _selectedLogDate = MutableStateFlow(dateFormat.format(Date()))
    val selectedLogDate: StateFlow<String> = _selectedLogDate.asStateFlow()

    private val _dailyLogRows = MutableStateFlow<List<AttendanceRowItem>>(emptyList())
    val dailyLogRows: StateFlow<List<AttendanceRowItem>> = _dailyLogRows.asStateFlow()

    // Monthly Report State
    private val _selectedReportMonth = MutableStateFlow(monthFormat.format(Date()))
    val selectedReportMonth: StateFlow<String> = _selectedReportMonth.asStateFlow()

    private val _monthlyReportRows = MutableStateFlow<List<MonthlyReportRow>>(emptyList())
    val monthlyReportRows: StateFlow<List<MonthlyReportRow>> = _monthlyReportRows.asStateFlow()

    init {
        // Auto-login default admin for testing or allow explicit login
        viewModelScope.launch {
            loadDashboardStats()
            loadDailyLog()
            loadMonthlyReport()
        }
    }

    fun login(username: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.authenticate(username.trim(), pass)
            if (user != null) {
                _currentUser.value = user
                _currentScreen.value = AppScreen.DASHBOARD
                loadDashboardStats()
                loadDailyLog()
                onResult(true, "مرحباً بك، ${user.fullName}")
            } else {
                onResult(false, "تحقق من اسم المستخدم وكلمة المرور.")
            }
        }
    }

    fun logout() {
        _currentUser.value?.let {
            viewModelScope.launch {
                repository.logAudit(it.username, "تسجيل الخروج", "")
                _currentUser.value = null
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        if (screen == AppScreen.DASHBOARD) loadDashboardStats()
        if (screen == AppScreen.DAILY_LOG) loadDailyLog()
        if (screen == AppScreen.REPORTS) loadMonthlyReport()
    }

    fun loadDashboardStats() {
        viewModelScope.launch {
            _dashboardStats.value = repository.getDashboardStats()
        }
    }

    fun setLogDate(date: String) {
        _selectedLogDate.value = date
        loadDailyLog()
    }

    fun loadDailyLog() {
        viewModelScope.launch {
            _dailyLogRows.value = repository.getDailyAttendanceRows(_selectedLogDate.value)
        }
    }

    fun setReportMonth(month: String) {
        _selectedReportMonth.value = month
        loadMonthlyReport()
    }

    fun loadMonthlyReport() {
        viewModelScope.launch {
            _monthlyReportRows.value = repository.getMonthlyReport(_selectedReportMonth.value)
        }
    }

    // Punch Clock
    fun punch(
        employeeId: Long,
        workDate: String,
        time24: String,
        shiftId: Long?,
        isCheckIn: Boolean,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val user = _currentUser.value?.username ?: "system"
            val result = repository.recordPunch(employeeId, workDate, time24, shiftId, isCheckIn, user)
            result.fold(
                onSuccess = { msg ->
                    loadDashboardStats()
                    loadDailyLog()
                    onComplete(true, msg)
                },
                onFailure = { err ->
                    onComplete(false, err.message ?: "حدث خطأ أثناء تسجيل الحركة")
                }
            )
        }
    }

    // Employee CRUD
    fun saveEmployee(
        id: Long?,
        code: String,
        name: String,
        phone: String,
        deptId: Long?,
        shiftId: Long?,
        hireDate: String,
        status: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val username = _currentUser.value?.username ?: "system"
            try {
                if (id != null && id > 0) {
                    val existing = db.employeeDao().getById(id)
                    if (existing != null) {
                        db.employeeDao().update(
                            existing.copy(
                                empCode = code,
                                fullName = name,
                                phone = phone,
                                departmentId = deptId,
                                shiftId = shiftId,
                                hireDate = hireDate,
                                status = status
                            )
                        )
                        repository.logAudit(username, "تعديل بيانات موظف", "ID=$id | $name ($code)")
                        onComplete(true, "تم تعديل بيانات الموظف بنجاح.")
                    } else {
                        onComplete(false, "الموظف غير موجود.")
                    }
                } else {
                    db.employeeDao().insert(
                        Employee(
                            empCode = code,
                            fullName = name,
                            phone = phone,
                            departmentId = deptId,
                            shiftId = shiftId,
                            hireDate = hireDate,
                            status = status
                        )
                    )
                    repository.logAudit(username, "إضافة موظف", "$name ($code)")
                    onComplete(true, "تمت إضافة الموظف بنجاح.")
                }
                loadDashboardStats()
            } catch (e: Exception) {
                onComplete(false, "تعذر الحفظ: قد يكون الرقم الوظيفي مستخدماً مسبقاً.")
            }
        }
    }

    fun deleteEmployee(emp: Employee, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val username = _currentUser.value?.username ?: "system"
            try {
                db.attendanceDao().deleteByEmployee(emp.id)
                db.leaveDao().deleteByEmployee(emp.id)
                db.employeeDao().delete(emp)
                repository.logAudit(username, "حذف موظف نهائياً", "ID=${emp.id} | ${emp.fullName}")
                loadDashboardStats()
                loadDailyLog()
                onComplete(true, "تم حذف الموظف وسجلاته بنجاح.")
            } catch (e: Exception) {
                onComplete(false, "تعذر حذف الموظف: ${e.message}")
            }
        }
    }

    // Departments CRUD
    fun saveDepartment(id: Long?, name: String, desc: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val username = _currentUser.value?.username ?: "system"
            try {
                if (id != null && id > 0) {
                    val dep = db.departmentDao().getById(id) ?: return@launch onComplete(false, "القسم غير موجود")
                    db.departmentDao().update(dep.copy(name = name, description = desc))
                    repository.logAudit(username, "تعديل قسم", name)
                } else {
                    db.departmentDao().insert(Department(name = name, description = desc))
                    repository.logAudit(username, "إضافة قسم", name)
                }
                loadDashboardStats()
                onComplete(true, "تم حفظ بيانات القسم.")
            } catch (e: Exception) {
                onComplete(false, "اسم القسم موجود مسبقاً.")
            }
        }
    }

    fun deleteDepartment(dept: Department, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val username = _currentUser.value?.username ?: "system"
            try {
                db.departmentDao().delete(dept)
                repository.logAudit(username, "حذف قسم", dept.name)
                loadDashboardStats()
                onComplete(true, "تم حذف القسم وفصل الموظفين عنه.")
            } catch (e: Exception) {
                onComplete(false, "تعذر حذف القسم: ${e.message}")
            }
        }
    }

    // Shifts CRUD
    fun saveShift(id: Long?, name: String, start: String, end: String, grace: Int, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val username = _currentUser.value?.username ?: "system"
            try {
                if (id != null && id > 0) {
                    val s = db.shiftDao().getById(id) ?: return@launch onComplete(false, "الوردية غير موجودة")
                    db.shiftDao().update(s.copy(name = name, startTime = start, endTime = end, graceMinutes = grace))
                    repository.logAudit(username, "تعديل وردية", name)
                } else {
                    db.shiftDao().insert(Shift(name = name, startTime = start, endTime = end, graceMinutes = grace))
                    repository.logAudit(username, "إضافة وردية", name)
                }
                onComplete(true, "تم حفظ الوردية بنجاح.")
            } catch (e: Exception) {
                onComplete(false, "تعذر الحفظ: تأكد من صحة البيانات وعدم تكرار اسم الوردية.")
            }
        }
    }

    fun deleteShift(shift: Shift, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val username = _currentUser.value?.username ?: "system"
            try {
                db.shiftDao().delete(shift)
                repository.logAudit(username, "حذف وردية", shift.name)
                onComplete(true, "تم حذف الوردية.")
            } catch (e: Exception) {
                onComplete(false, "تعذر الحذف: هذه الوردية مرتبطة بموظفين أو سجلات حضور.")
            }
        }
    }

    // Leaves CRUD
    fun saveLeave(
        id: Long?,
        empId: Long,
        start: String,
        end: String,
        type: String,
        reason: String,
        status: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val username = _currentUser.value?.username ?: "system"
            try {
                if (end < start) {
                    return@launch onComplete(false, "تاريخ النهاية يجب ألا يسبق تاريخ البداية.")
                }
                val overlap = db.leaveDao().findOverlap(empId, start, end, id ?: -1)
                if (overlap != null) {
                    return@launch onComplete(false, "توجد إجازة أخرى متداخلة لهذا الموظف في نفس الفترة.")
                }
                if (id != null && id > 0) {
                    val lv = db.leaveDao().getById(id) ?: return@launch onComplete(false, "طلب الإجازة غير موجود")
                    db.leaveDao().update(
                        lv.copy(
                            employeeId = empId,
                            startDate = start,
                            endDate = end,
                            leaveType = type,
                            reason = reason,
                            status = status
                        )
                    )
                    repository.logAudit(username, "تعديل طلب إجازة", "ID=$id ($status)")
                } else {
                    db.leaveDao().insert(
                        LeaveRequest(
                            employeeId = empId,
                            startDate = start,
                            endDate = end,
                            leaveType = type,
                            reason = reason,
                            status = status
                        )
                    )
                    repository.logAudit(username, "إضافة طلب إجازة", "EmpID=$empId")
                }
                loadDailyLog()
                onComplete(true, "تم حفظ طلب الإجازة.")
            } catch (e: Exception) {
                onComplete(false, "حدث خطأ: ${e.message}")
            }
        }
    }

    fun deleteLeave(leave: LeaveRequest, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val username = _currentUser.value?.username ?: "system"
            try {
                db.leaveDao().delete(leave)
                repository.logAudit(username, "حذف طلب إجازة", "ID=${leave.id}")
                loadDailyLog()
                onComplete(true, "تم حذف طلب الإجازة.")
            } catch (e: Exception) {
                onComplete(false, "تعذر الحذف: ${e.message}")
            }
        }
    }

    // Workdays & Holidays
    fun saveWorkdays(days: List<WorkDay>, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val username = _currentUser.value?.username ?: "system"
            db.workdayDao().insertAll(days)
            repository.logAudit(username, "تعديل أيام العمل", "")
            loadDashboardStats()
            loadDailyLog()
            onComplete(true, "تم حفظ أيام العمل.")
        }
    }

    fun addHoliday(date: String, name: String, notes: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val username = _currentUser.value?.username ?: "system"
            try {
                db.holidayDao().insert(Holiday(holidayDate = date, name = name, notes = notes))
                repository.logAudit(username, "إضافة عطلة", "$name ($date)")
                loadDashboardStats()
                loadDailyLog()
                onComplete(true, "تمت إضافة العطلة.")
            } catch (e: Exception) {
                onComplete(false, "تأكد من عدم تكرار تاريخ العطلة.")
            }
        }
    }

    fun deleteHoliday(holiday: Holiday, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val username = _currentUser.value?.username ?: "system"
            db.holidayDao().delete(holiday)
            repository.logAudit(username, "حذف عطلة", "${holiday.name} (${holiday.holidayDate})")
            loadDashboardStats()
            loadDailyLog()
            onComplete(true, "تم حذف العطلة.")
        }
    }

    // Users CRUD
    fun saveUser(
        id: Long?,
        username: String,
        fullName: String,
        role: String,
        active: Boolean,
        passwordRaw: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val adminUsername = _currentUser.value?.username ?: "system"
            try {
                if (id != null && id > 0) {
                    val u = db.userDao().getById(id) ?: return@launch onComplete(false, "المستخدم غير موجود")
                    val newHash = if (passwordRaw.isNotBlank()) AppDatabase.hashSha256(passwordRaw) else u.passwordHash
                    db.userDao().update(
                        u.copy(
                            username = username,
                            fullName = fullName,
                            role = role,
                            active = active,
                            passwordHash = newHash
                        )
                    )
                    repository.logAudit(adminUsername, "تعديل مستخدم", username)
                } else {
                    if (passwordRaw.length < 8) {
                        return@launch onComplete(false, "كلمة المرور يجب أن تكون 8 أحرف على الأقل.")
                    }
                    val hash = AppDatabase.hashSha256(passwordRaw)
                    db.userDao().insert(
                        User(
                            username = username,
                            fullName = fullName,
                            role = role,
                            active = active,
                            passwordHash = hash
                        )
                    )
                    repository.logAudit(adminUsername, "إضافة مستخدم", username)
                }
                onComplete(true, "تم حفظ بيانات الحساب بنجاح.")
            } catch (e: Exception) {
                onComplete(false, "اسم المستخدم مسجل مسبقاً. اختر اسماً آخر.")
            }
        }
    }

    fun deactivateUser(user: User, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val admin = _currentUser.value
            if (user.id == admin?.id) {
                return@launch onComplete(false, "لا يمكنك تعطيل حسابك الحالي.")
            }
            db.userDao().deactivate(user.id)
            repository.logAudit(admin?.username ?: "system", "تعطيل مستخدم", user.username)
            onComplete(true, "تم تعطيل الحساب.")
        }
    }
}
