package com.example.attendance.data.repository

import com.example.attendance.data.db.AppDatabase
import com.example.attendance.data.model.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

data class AttendanceRowItem(
    val employeeId: Long,
    val attendanceId: Long,
    val empCode: String,
    val empName: String,
    val shiftName: String,
    val checkIn12: String,
    val checkOut12: String,
    val totalHours: String,
    val lateMinutes: String,
    val earlyMinutes: String,
    val overtimeMinutes: String,
    val status: String
)

data class MonthlyReportRow(
    val empCode: String,
    val empName: String,
    val presentDays: Int,
    val absentDays: Int,
    val leaveDays: Int,
    val totalLateMinutes: Int,
    val totalEarlyMinutes: Int,
    val totalOvertimeMinutes: Int,
    val totalHours: String
)

data class DashboardStats(
    val totalEmployees: Int,
    val presentToday: Int,
    val absentToday: Int,
    val openPunchCount: Int,
    val pendingLeavesCount: Int,
    val departmentsCount: Int
)

class AttendanceRepository(private val db: AppDatabase) {
    val usersFlow: Flow<List<User>> = db.userDao().getAllUsersFlow()
    val departmentsFlow: Flow<List<Department>> = db.departmentDao().getAllDepartmentsFlow()
    val shiftsFlow: Flow<List<Shift>> = db.shiftDao().getAllShiftsFlow()
    val employeesFlow: Flow<List<Employee>> = db.employeeDao().getAllEmployeesFlow()
    val leavesFlow: Flow<List<LeaveRequest>> = db.leaveDao().getAllLeavesFlow()
    val workdaysFlow: Flow<List<WorkDay>> = db.workdayDao().getWorkdaysFlow()
    val holidaysFlow: Flow<List<Holiday>> = db.holidayDao().getAllHolidaysFlow()
    val auditLogsFlow: Flow<List<AuditLog>> = db.auditDao().getAuditLogsFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val time24Format = SimpleDateFormat("HH:mm", Locale.US)
    private val time12Format = SimpleDateFormat("hh:mm a", Locale.US)
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    suspend fun logAudit(username: String, action: String, details: String = "") {
        val now = dateTimeFormat.format(Date())
        db.auditDao().insert(AuditLog(username = username, action = action, details = details, createdAt = now))
    }

    suspend fun authenticate(username: String, passwordRaw: String): User? {
        val user = db.userDao().getByUsername(username) ?: return null
        if (!user.active) return null
        val hash = AppDatabase.hashSha256(passwordRaw)
        return if (user.passwordHash == hash || user.passwordHash == passwordRaw) {
            logAudit(user.username, "تسجيل الدخول", "نجاح تسجيل الدخول")
            user
        } else {
            null
        }
    }

    // Format display helpers
    fun formatTo12Hour(dateTimeStr: String?): String {
        if (dateTimeStr.isNullOrBlank()) return "—"
        return try {
            val date = dateTimeFormat.parse(dateTimeStr) ?: return dateTimeStr
            time12Format.format(date)
        } catch (_: Exception) {
            dateTimeStr
        }
    }

    suspend fun getDashboardStats(): DashboardStats {
        val todayStr = dateFormat.format(Date())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dateFormat.format(cal.time)

        val activeEmployees = db.employeeDao().getActiveEmployees().filter {
            it.hireDate.isBlank() || it.hireDate <= todayStr
        }
        val totalActive = activeEmployees.size

        val todayAttendance = db.attendanceDao().getAttendanceByDate(todayStr)
        val presentIds = todayAttendance.filter { !it.checkIn.isNullOrBlank() }.map { it.employeeId }.toSet()

        val recentAtt = db.attendanceDao().getAttendanceBetween(yesterdayStr, todayStr)
        val openPunchCount = recentAtt.filter { !it.checkIn.isNullOrBlank() && it.checkOut.isNullOrBlank() }
            .map { it.employeeId }.toSet().size

        val allLeaves = db.leaveDao().getAllLeavesFlow()
        // For leaves, fetch approved for today
        val approvedLeavesToday = db.leaveDao().getApprovedLeavesBetween(todayStr, todayStr)
            .map { it.employeeId }.toSet()

        val workdaysMap = db.workdayDao().getWorkdays().associate { it.weekday to it.isWorkday }
        val holidaysSet = db.holidayDao().getAllHolidays().map { it.holidayDate }.toSet()

        val todayCal = Calendar.getInstance()
        // Java Calendar weekday: Sunday = 1, Mon = 2 ... Sat = 7
        val dayOfWeekIndex = (todayCal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // 0=Mon .. 6=Sun
        val isTodayWorkday = (workdaysMap[dayOfWeekIndex] ?: true) && !holidaysSet.contains(todayStr)

        val absentCount = if (isTodayWorkday) {
            max(0, totalActive - (presentIds + approvedLeavesToday).size)
        } else 0

        // For pending leaves
        val pendingLeaves = db.leaveDao().getApprovedLeavesBetween("1970-01-01", "2099-12-31")
        // we can count pending from direct query or approximate
        val allLeavesList = activeEmployees.size // placeholder
        val departmentsCount = db.departmentDao().getAllDepartments().size

        return DashboardStats(
            totalEmployees = totalActive,
            presentToday = presentIds.size,
            absentToday = absentCount,
            openPunchCount = openPunchCount,
            pendingLeavesCount = 0,
            departmentsCount = departmentsCount
        )
    }

    suspend fun getDailyAttendanceRows(workDate: String): List<AttendanceRowItem> {
        val employees = db.employeeDao().getActiveEmployees()
        val shifts = db.shiftDao().getAllShifts().associateBy { it.id }
        val attendance = db.attendanceDao().getAttendanceByDate(workDate)
        val approvedLeaves = db.leaveDao().getApprovedLeavesBetween(workDate, workDate)
            .map { it.employeeId }.toSet()
        val workdaysMap = db.workdayDao().getWorkdays().associate { it.weekday to it.isWorkday }
        val holidaysSet = db.holidayDao().getAllHolidays().map { it.holidayDate }.toSet()

        val dateObj = dateFormat.parse(workDate) ?: Date()
        val cal = Calendar.getInstance().apply { time = dateObj }
        val weekdayIdx = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        val isWorkday = (workdaysMap[weekdayIdx] ?: true) && !holidaysSet.contains(workDate)

        val grouped = attendance.groupBy { it.employeeId }
        val result = mutableListOf<AttendanceRowItem>()

        for (emp in employees) {
            val records = grouped[emp.id]
            if (!records.isNullOrEmpty()) {
                for (rec in records) {
                    val shift = rec.shiftId?.let { shifts[it] } ?: emp.shiftId?.let { shifts[it] }
                    val checkIn = rec.checkIn
                    val checkOut = rec.checkOut

                    var totalHours = "—"
                    var lateMin = "—"
                    var earlyMin = "—"
                    var overtimeMin = "—"

                    val state = when {
                        approvedLeaves.contains(emp.id) -> "إجازة"
                        !checkOut.isNullOrBlank() -> "حاضر"
                        !checkIn.isNullOrBlank() -> "لم يسجل الانصراف"
                        else -> "سجل فارغ"
                    }

                    if (!checkIn.isNullOrBlank()) {
                        shift?.let { s ->
                            try {
                                val shiftStartCal = Calendar.getInstance().apply {
                                    time = dateFormat.parse(workDate) ?: Date()
                                    val parts = s.startTime.split(":")
                                    set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                                    set(Calendar.MINUTE, parts[1].toInt())
                                    set(Calendar.SECOND, 0)
                                    add(Calendar.MINUTE, s.graceMinutes)
                                }
                                val actualInDate = dateTimeFormat.parse(checkIn)
                                if (actualInDate != null) {
                                    val diffSec = (actualInDate.time - shiftStartCal.time.time) / 1000
                                    val diffMin = max(0, (diffSec / 60).toInt())
                                    lateMin = diffMin.toString()
                                }
                            } catch (_: Exception) {}
                        }
                    }

                    if (!checkIn.isNullOrBlank() && !checkOut.isNullOrBlank()) {
                        try {
                            val inDate = dateTimeFormat.parse(checkIn)
                            val outDate = dateTimeFormat.parse(checkOut)
                            if (inDate != null && outDate != null) {
                                var durationSec = (outDate.time - inDate.time) / 1000
                                if (durationSec < 0) durationSec += 86400 // night shift cross midnight
                                totalHours = String.format(Locale.US, "%.2f", max(0.0, durationSec / 3600.0))

                                shift?.let { s ->
                                    val shiftEndCal = Calendar.getInstance().apply {
                                        time = dateFormat.parse(workDate) ?: Date()
                                        val parts = s.endTime.split(":")
                                        set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                                        set(Calendar.MINUTE, parts[1].toInt())
                                        set(Calendar.SECOND, 0)
                                    }
                                    if (s.endTime <= s.startTime) {
                                        shiftEndCal.add(Calendar.DAY_OF_YEAR, 1)
                                    }
                                    val diffSec = (outDate.time - shiftEndCal.time.time) / 1000
                                    val diffMin = (diffSec / 60).toInt()
                                    if (diffMin < 0) {
                                        earlyMin = (-diffMin).toString()
                                    } else {
                                        overtimeMin = diffMin.toString()
                                    }
                                }
                            }
                        } catch (_: Exception) {}
                    }

                    result.add(
                        AttendanceRowItem(
                            employeeId = emp.id,
                            attendanceId = rec.id,
                            empCode = emp.empCode,
                            empName = emp.fullName,
                            shiftName = shift?.name ?: "—",
                            checkIn12 = formatTo12Hour(checkIn),
                            checkOut12 = formatTo12Hour(checkOut),
                            totalHours = totalHours,
                            lateMinutes = lateMin,
                            earlyMinutes = earlyMin,
                            overtimeMinutes = overtimeMin,
                            status = state
                        )
                    )
                }
            } else {
                val state = when {
                    approvedLeaves.contains(emp.id) -> "إجازة"
                    isWorkday -> "غياب"
                    else -> "عطلة"
                }
                val shift = emp.shiftId?.let { shifts[it] }
                result.add(
                    AttendanceRowItem(
                        employeeId = emp.id,
                        attendanceId = 0,
                        empCode = emp.empCode,
                        empName = emp.fullName,
                        shiftName = shift?.name ?: "—",
                        checkIn12 = "—",
                        checkOut12 = "—",
                        totalHours = "—",
                        lateMinutes = "—",
                        earlyMinutes = "—",
                        overtimeMinutes = "—",
                        status = state
                    )
                )
            }
        }
        return result
    }

    suspend fun recordPunch(
        employeeId: Long,
        workDate: String,
        time24: String, // "08:30"
        shiftId: Long?,
        isCheckIn: Boolean,
        username: String
    ): Result<String> {
        val emp = db.employeeDao().getById(employeeId) ?: return Result.failure(Exception("الموظف غير موجود"))
        if (emp.status != "نشط") return Result.failure(Exception("الموظف غير نشط"))

        val stamp = "$workDate $time24:00"

        return if (isCheckIn) {
            val sid = shiftId ?: emp.shiftId ?: return Result.failure(Exception("الوردية مطلوبة لتسجيل الحضور"))
            val existing = db.attendanceDao().getRecordForShift(employeeId, workDate, sid)
            if (existing != null && !existing.checkIn.isNullOrBlank()) {
                return Result.failure(Exception("يوجد تسجيل حضور مسجل لهذا الموظف في نفس التاريخ والوردية"))
            }
            if (existing != null) {
                db.attendanceDao().update(existing.copy(checkIn = stamp))
            } else {
                db.attendanceDao().insert(
                    AttendanceRecord(
                        employeeId = employeeId,
                        workDate = workDate,
                        checkIn = stamp,
                        shiftId = sid
                    )
                )
            }
            logAudit(username, "تسجيل حضور", "${emp.fullName} ($workDate $time24)")
            Result.success("تم تسجيل الحضور للموظف ${emp.fullName} بنجاح")
        } else {
            // Find open attendance
            val cal = Calendar.getInstance().apply { time = dateFormat.parse(workDate) ?: Date() }
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val prevDate = dateFormat.format(cal.time)

            val openRecords = db.attendanceDao().findOpenAttendance(employeeId, prevDate, workDate)
            val target = openRecords.firstOrNull() ?: return Result.failure(Exception("لا يوجد تسجيل حضور مفتوح لهذا الموظف في هذا اليوم أو اليوم السابق"))

            db.attendanceDao().update(target.copy(checkOut = stamp))
            logAudit(username, "تسجيل انصراف", "${emp.fullName} ($workDate $time24)")
            Result.success("تم تسجيل الانصراف للموظف ${emp.fullName} بنجاح")
        }
    }

    suspend fun getMonthlyReport(monthStr: String): List<MonthlyReportRow> {
        val cal = Calendar.getInstance().apply {
            time = SimpleDateFormat("yyyy-MM", Locale.US).parse(monthStr) ?: Date()
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val startDate = "$monthStr-01"
        val endDate = "$monthStr-${String.format(Locale.US, "%02d", maxDays)}"

        val employees = db.employeeDao().getActiveEmployees()
        val shifts = db.shiftDao().getAllShifts().associateBy { it.id }
        val attendance = db.attendanceDao().getAttendanceBetween(startDate, endDate)
        val leaves = db.leaveDao().getApprovedLeavesBetween(startDate, endDate)
        val workdaysMap = db.workdayDao().getWorkdays().associate { it.weekday to it.isWorkday }
        val holidaysSet = db.holidayDao().getAllHolidays().map { it.holidayDate }.toSet()

        val attMap = attendance.groupBy { Pair(it.employeeId, it.workDate) }
        val result = mutableListOf<MonthlyReportRow>()

        for (emp in employees) {
            var presentDays = 0
            var absentDays = 0
            var leaveDays = 0
            var lateMinutesTotal = 0
            var earlyMinutesTotal = 0
            var overtimeMinutesTotal = 0
            var totalSecondsWorked = 0L

            for (day in 1..maxDays) {
                val dayStr = "$monthStr-${String.format(Locale.US, "%02d", day)}"
                if (emp.hireDate.isNotBlank() && dayStr < emp.hireDate) continue

                val dayDate = dateFormat.parse(dayStr) ?: continue
                val dayCal = Calendar.getInstance().apply { time = dayDate }
                val weekdayIdx = (dayCal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                val isWork = (workdaysMap[weekdayIdx] ?: true) && !holidaysSet.contains(dayStr)
                if (!isWork) continue

                val onLeave = leaves.any { it.employeeId == emp.id && it.startDate <= dayStr && it.endDate >= dayStr }
                if (onLeave) {
                    leaveDays++
                    continue
                }

                val records = attMap[Pair(emp.id, dayStr)]
                if (!records.isNullOrEmpty() && records.any { !it.checkIn.isNullOrBlank() }) {
                    presentDays++
                    for (rec in records) {
                        val shift = rec.shiftId?.let { shifts[it] } ?: emp.shiftId?.let { shifts[it] }
                        if (!rec.checkIn.isNullOrBlank() && !rec.checkOut.isNullOrBlank()) {
                            try {
                                val inD = dateTimeFormat.parse(rec.checkIn)
                                val outD = dateTimeFormat.parse(rec.checkOut)
                                if (inD != null && outD != null) {
                                    var dur = (outD.time - inD.time) / 1000
                                    if (dur < 0) dur += 86400
                                    totalSecondsWorked += max(0, dur)

                                    shift?.let { s ->
                                        val shiftEndCal = Calendar.getInstance().apply {
                                            time = dayDate
                                            val parts = s.endTime.split(":")
                                            set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                                            set(Calendar.MINUTE, parts[1].toInt())
                                            set(Calendar.SECOND, 0)
                                        }
                                        if (s.endTime <= s.startTime) shiftEndCal.add(Calendar.DAY_OF_YEAR, 1)
                                        val diff = ((outD.time - shiftEndCal.time.time) / 1000 / 60).toInt()
                                        if (diff < 0) earlyMinutesTotal += -diff else overtimeMinutesTotal += diff
                                    }
                                }
                            } catch (_: Exception) {}
                        }

                        if (!rec.checkIn.isNullOrBlank()) {
                            shift?.let { s ->
                                try {
                                    val shiftStartCal = Calendar.getInstance().apply {
                                        time = dayDate
                                        val parts = s.startTime.split(":")
                                        set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                                        set(Calendar.MINUTE, parts[1].toInt())
                                        set(Calendar.SECOND, 0)
                                        add(Calendar.MINUTE, s.graceMinutes)
                                    }
                                    val inD = dateTimeFormat.parse(rec.checkIn)
                                    if (inD != null) {
                                        val diff = ((inD.time - shiftStartCal.time.time) / 1000 / 60).toInt()
                                        if (diff > 0) lateMinutesTotal += diff
                                    }
                                } catch (_: Exception) {}
                            }
                        }
                    }
                } else {
                    absentDays++
                }
            }

            val hoursStr = String.format(Locale.US, "%.2f", totalSecondsWorked / 3600.0)
            result.add(
                MonthlyReportRow(
                    empCode = emp.empCode,
                    empName = emp.fullName,
                    presentDays = presentDays,
                    absentDays = absentDays,
                    leaveDays = leaveDays,
                    totalLateMinutes = lateMinutesTotal,
                    totalEarlyMinutes = earlyMinutesTotal,
                    totalOvertimeMinutes = overtimeMinutesTotal,
                    totalHours = hoursStr
                )
            )
        }
        return result
    }
}
