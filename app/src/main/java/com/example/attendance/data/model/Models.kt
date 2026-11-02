package com.example.attendance.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    @ColumnInfo(name = "password_hash") val passwordHash: String,
    @ColumnInfo(name = "full_name") val fullName: String,
    val role: String = "مدير", // 'مدير' (Admin), 'مسؤول' (Supervisor), 'مستخدم' (User)
    val active: Boolean = true
)

@Entity(
    tableName = "departments",
    indices = [Index(value = ["name"], unique = true)]
)
data class Department(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = ""
)

@Entity(
    tableName = "shifts",
    indices = [Index(value = ["name"], unique = true)]
)
data class Shift(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "start_time") val startTime: String, // "08:00" (24h)
    @ColumnInfo(name = "end_time") val endTime: String,     // "16:00" (24h)
    @ColumnInfo(name = "grace_minutes") val graceMinutes: Int = 10
)

@Entity(
    tableName = "employees",
    indices = [
        Index(value = ["emp_code"], unique = true),
        Index(value = ["department_id"]),
        Index(value = ["shift_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Department::class,
            parentColumns = ["id"],
            childColumns = ["department_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Shift::class,
            parentColumns = ["id"],
            childColumns = ["shift_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Employee(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "emp_code") val empCode: String,
    @ColumnInfo(name = "full_name") val fullName: String,
    val phone: String = "",
    @ColumnInfo(name = "department_id") val departmentId: Long? = null,
    @ColumnInfo(name = "shift_id") val shiftId: Long? = null,
    @ColumnInfo(name = "hire_date") val hireDate: String = "", // "YYYY-MM-DD"
    val status: String = "نشط" // "نشط", "غير نشط"
)

@Entity(
    tableName = "attendance",
    indices = [
        Index(value = ["work_date"]),
        Index(value = ["employee_id", "work_date", "shift_id"], unique = true),
        Index(value = ["shift_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Employee::class,
            parentColumns = ["id"],
            childColumns = ["employee_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Shift::class,
            parentColumns = ["id"],
            childColumns = ["shift_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "employee_id") val employeeId: Long,
    @ColumnInfo(name = "work_date") val workDate: String, // "YYYY-MM-DD"
    @ColumnInfo(name = "check_in") val checkIn: String? = null, // "YYYY-MM-DD HH:mm:ss"
    @ColumnInfo(name = "check_out") val checkOut: String? = null, // "YYYY-MM-DD HH:mm:ss"
    val notes: String = "",
    @ColumnInfo(name = "shift_id") val shiftId: Long? = null
)

@Entity(
    tableName = "leave_requests",
    indices = [Index(value = ["employee_id"])],
    foreignKeys = [
        ForeignKey(
            entity = Employee::class,
            parentColumns = ["id"],
            childColumns = ["employee_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class LeaveRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "employee_id") val employeeId: Long,
    @ColumnInfo(name = "start_date") val startDate: String, // "YYYY-MM-DD"
    @ColumnInfo(name = "end_date") val endDate: String,     // "YYYY-MM-DD"
    @ColumnInfo(name = "leave_type") val leaveType: String, // "سنوية", "مرضية", "طارئة", "بدون راتب", "إذن"
    val reason: String = "",
    val status: String = "قيد المراجعة" // "قيد المراجعة", "موافق", "مرفوض"
)

@Entity(tableName = "workdays")
data class WorkDay(
    @PrimaryKey val weekday: Int, // 0 = Monday, 6 = Sunday (or 0=Mon .. 4=Fri, 5=Sat)
    @ColumnInfo(name = "is_workday") val isWorkday: Boolean = true
)

@Entity(
    tableName = "holidays",
    indices = [Index(value = ["holiday_date"], unique = true)]
)
data class Holiday(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "holiday_date") val holidayDate: String, // "YYYY-MM-DD"
    val name: String,
    val notes: String = ""
)

@Entity(tableName = "audit_log")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val action: String,
    val details: String = "",
    @ColumnInfo(name = "created_at") val createdAt: String
)
