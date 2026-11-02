package com.example.attendance.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.attendance.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest

@Database(
    entities = [
        User::class,
        Department::class,
        Shift::class,
        Employee::class,
        AttendanceRecord::class,
        LeaveRequest::class,
        WorkDay::class,
        Holiday::class,
        AuditLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun departmentDao(): DepartmentDao
    abstract fun shiftDao(): ShiftDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun leaveDao(): LeaveDao
    abstract fun workdayDao(): WorkdayDao
    abstract fun holidayDao(): HolidayDao
    abstract fun auditDao(): AuditDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "attendance.db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun hashSha256(text: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(text.toByteArray(Charsets.UTF_8))
            return hashBytes.joinToString("") { "%02x".format(it) }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        // Pre-populate initial workdays (Fri & Sat weekend, others workday)
                        val workdays = (0..6).map { i ->
                            WorkDay(weekday = i, isWorkday = (i != 4 && i != 5))
                        }
                        database.workdayDao().insertAll(workdays)

                        // Pre-populate default admin user
                        val adminHash = hashSha256("Admin@123")
                        database.userDao().insert(
                            User(
                                username = "admin",
                                passwordHash = adminHash,
                                fullName = "مدير النظام",
                                role = "مدير",
                                active = true
                            )
                        )

                        // Pre-populate default departments
                        database.departmentDao().insert(Department(name = "الإدارة", description = "الإدارة العامة"))
                        database.departmentDao().insert(Department(name = "تقنية المعلومات", description = "قسم التقنية"))
                        database.departmentDao().insert(Department(name = "المحاسبة", description = "الشؤون المالية"))

                        // Pre-populate default shifts
                        database.shiftDao().insert(
                            Shift(name = "الدوام الصباحي", startTime = "08:00", endTime = "16:00", graceMinutes = 10)
                        )
                        database.shiftDao().insert(
                            Shift(name = "الدوام المسائي", startTime = "16:00", endTime = "00:00", graceMinutes = 10)
                        )
                    }
                }
            }
        }
    }
}
