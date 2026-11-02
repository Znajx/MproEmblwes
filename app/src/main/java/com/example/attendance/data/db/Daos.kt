package com.example.attendance.data.db

import androidx.room.*
import com.example.attendance.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsersFlow(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): User?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Query("UPDATE users SET active = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)
}

@Dao
interface DepartmentDao {
    @Query("SELECT * FROM departments ORDER BY name COLLATE NOCASE ASC")
    fun getAllDepartmentsFlow(): Flow<List<Department>>

    @Query("SELECT * FROM departments ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAllDepartments(): List<Department>

    @Query("SELECT * FROM departments WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Department?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(department: Department): Long

    @Update
    suspend fun update(department: Department)

    @Delete
    suspend fun delete(department: Department)
}

@Dao
interface ShiftDao {
    @Query("SELECT * FROM shifts ORDER BY name COLLATE NOCASE ASC")
    fun getAllShiftsFlow(): Flow<List<Shift>>

    @Query("SELECT * FROM shifts ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAllShifts(): List<Shift>

    @Query("SELECT * FROM shifts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Shift?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(shift: Shift): Long

    @Update
    suspend fun update(shift: Shift)

    @Delete
    suspend fun delete(shift: Shift)
}

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY full_name COLLATE NOCASE ASC")
    fun getAllEmployeesFlow(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE status = 'نشط' ORDER BY full_name COLLATE NOCASE ASC")
    suspend fun getActiveEmployees(): List<Employee>

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Employee?

    @Query("SELECT * FROM employees WHERE emp_code = :code LIMIT 1")
    suspend fun getByCode(code: String): Employee?

    @Query("SELECT * FROM employees WHERE full_name LIKE '%' || :query || '%' OR emp_code LIKE '%' || :query || '%'")
    suspend fun search(query: String): List<Employee>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(employee: Employee): Long

    @Update
    suspend fun update(employee: Employee)

    @Delete
    suspend fun delete(employee: Employee)

    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE work_date = :workDate ORDER BY employee_id, id")
    fun getAttendanceByDateFlow(workDate: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance WHERE work_date = :workDate")
    suspend fun getAttendanceByDate(workDate: String): List<AttendanceRecord>

    @Query("SELECT * FROM attendance WHERE work_date BETWEEN :startDate AND :endDate ORDER BY work_date, id")
    suspend fun getAttendanceBetween(startDate: String, endDate: String): List<AttendanceRecord>

    @Query("SELECT * FROM attendance WHERE employee_id = :employeeId AND check_in IS NOT NULL AND check_out IS NULL AND work_date BETWEEN :prevDate AND :currentDate ORDER BY work_date DESC, id DESC")
    suspend fun findOpenAttendance(employeeId: Long, prevDate: String, currentDate: String): List<AttendanceRecord>

    @Query("SELECT * FROM attendance WHERE employee_id = :employeeId AND work_date = :workDate AND shift_id = :shiftId LIMIT 1")
    suspend fun getRecordForShift(employeeId: Long, workDate: String, shiftId: Long): AttendanceRecord?

    @Query("SELECT * FROM attendance WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): AttendanceRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attendance: AttendanceRecord): Long

    @Update
    suspend fun update(attendance: AttendanceRecord)

    @Query("DELETE FROM attendance WHERE employee_id = :employeeId")
    suspend fun deleteByEmployee(employeeId: Long)
}

@Dao
interface LeaveDao {
    @Query("SELECT * FROM leave_requests ORDER BY start_date DESC, id DESC")
    fun getAllLeavesFlow(): Flow<List<LeaveRequest>>

    @Query("SELECT * FROM leave_requests WHERE status = 'موافق' AND start_date <= :endDate AND end_date >= :startDate")
    suspend fun getApprovedLeavesBetween(startDate: String, endDate: String): List<LeaveRequest>

    @Query("SELECT * FROM leave_requests WHERE employee_id = :employeeId AND status != 'مرفوض' AND start_date <= :endDate AND end_date >= :startDate AND id != :excludeId LIMIT 1")
    suspend fun findOverlap(employeeId: Long, startDate: String, endDate: String, excludeId: Long): LeaveRequest?

    @Query("SELECT * FROM leave_requests WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): LeaveRequest?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(leave: LeaveRequest): Long

    @Update
    suspend fun update(leave: LeaveRequest)

    @Delete
    suspend fun delete(leave: LeaveRequest)

    @Query("DELETE FROM leave_requests WHERE employee_id = :employeeId")
    suspend fun deleteByEmployee(employeeId: Long)
}

@Dao
interface WorkdayDao {
    @Query("SELECT * FROM workdays ORDER BY weekday ASC")
    fun getWorkdaysFlow(): Flow<List<WorkDay>>

    @Query("SELECT * FROM workdays ORDER BY weekday ASC")
    suspend fun getWorkdays(): List<WorkDay>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(workdays: List<WorkDay>)
}

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holidays ORDER BY holiday_date ASC")
    fun getAllHolidaysFlow(): Flow<List<Holiday>>

    @Query("SELECT * FROM holidays ORDER BY holiday_date ASC")
    suspend fun getAllHolidays(): List<Holiday>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(holiday: Holiday): Long

    @Delete
    suspend fun delete(holiday: Holiday)

    @Query("DELETE FROM holidays WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_log ORDER BY id DESC LIMIT 1000")
    fun getAuditLogsFlow(): Flow<List<AuditLog>>

    @Insert
    suspend fun insert(log: AuditLog): Long
}
