package com.example.attendance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.attendance.ui.screens.*
import com.example.attendance.ui.theme.*
import com.example.attendance.ui.viewmodel.AppScreen
import com.example.attendance.ui.viewmodel.AttendanceViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: AttendanceViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AttendanceTheme {
                // Enforce RTL layout for natural Arabic typography and flow
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
                    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                    val dashboardStats by viewModel.dashboardStats.collectAsStateWithLifecycle()
                    val employees by viewModel.employees.collectAsStateWithLifecycle()
                    val departments by viewModel.departments.collectAsStateWithLifecycle()
                    val shifts by viewModel.shifts.collectAsStateWithLifecycle()
                    val leaves by viewModel.leaves.collectAsStateWithLifecycle()
                    val workdays by viewModel.workdays.collectAsStateWithLifecycle()
                    val holidays by viewModel.holidays.collectAsStateWithLifecycle()
                    val users by viewModel.users.collectAsStateWithLifecycle()
                    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
                    val dailyRows by viewModel.dailyLogRows.collectAsStateWithLifecycle()
                    val currentLogDate by viewModel.selectedLogDate.collectAsStateWithLifecycle()
                    val currentReportMonth by viewModel.selectedReportMonth.collectAsStateWithLifecycle()
                    val reportRows by viewModel.monthlyReportRows.collectAsStateWithLifecycle()

                    if (currentUser == null) {
                        LoginScreen(
                            onLoginSuccess = { /* handled via currentUser state flow */ },
                            onLoginAttempt = { u, p, callback ->
                                viewModel.login(u, p, callback)
                            }
                        )
                    } else {
                        val userRole = currentUser?.role ?: "مستخدم"
                        // Filter screens by role
                        val accessibleScreens = AppScreen.values().filter { it.allowedRoles.contains(userRole) }

                        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                        val scope = rememberCoroutineScope()

                        // Handle device back button
                        BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
                            viewModel.navigateTo(AppScreen.DASHBOARD)
                        }

                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            drawerContent = {
                                ModalDrawerSheet(
                                    drawerContainerColor = NavyPrimary,
                                    modifier = Modifier.width(300.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp)
                                    ) {
                                        Text(
                                            text = "نظام الحضور والانصراف",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = GoldLight,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "المستخدم: ${currentUser?.fullName} (${currentUser?.role})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = PaperSurface.copy(alpha = 0.8f)
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        HorizontalDivider(color = GoldAccent.copy(alpha = 0.4f))
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Navigation Items
                                    accessibleScreens.forEach { screen ->
                                        val isSelected = screen == currentScreen
                                        NavigationDrawerItem(
                                            label = {
                                                Text(
                                                    text = screen.title,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) NavyPrimary else PaperSurface
                                                )
                                            },
                                            icon = {
                                                Icon(
                                                    getScreenIcon(screen),
                                                    contentDescription = null,
                                                    tint = if (isSelected) NavyPrimary else GoldLight
                                                )
                                            },
                                            selected = isSelected,
                                            onClick = {
                                                viewModel.navigateTo(screen)
                                                scope.launch { drawerState.close() }
                                            },
                                            colors = NavigationDrawerItemDefaults.colors(
                                                selectedContainerColor = GoldLight,
                                                unselectedContainerColor = NavyPrimary
                                            ),
                                            modifier = Modifier
                                                .padding(horizontal = 12.dp, vertical = 2.dp)
                                                .testTag("nav_item_${screen.name}")
                                        )
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    // Logout button
                                    Button(
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            viewModel.logout()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NavySecondary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                            .testTag("logout_button")
                                    ) {
                                        Icon(Icons.Default.Logout, contentDescription = null, tint = StatusRed)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("تسجيل الخروج", color = PaperSurface)
                                    }
                                }
                            }
                        ) {
                            Scaffold(
                                topBar = {
                                    TopAppBar(
                                        title = {
                                            Column {
                                                Text(
                                                    text = "نظام إدارة الحضور والانصراف",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GoldLight
                                                )
                                                Text(
                                                    text = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = PaperSurface.copy(alpha = 0.7f),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        },
                                        navigationIcon = {
                                            IconButton(
                                                onClick = { scope.launch { drawerState.open() } },
                                                modifier = Modifier.testTag("drawer_menu_button")
                                            ) {
                                                Icon(Icons.Default.Menu, contentDescription = "فتح القائمة", tint = GoldLight)
                                            }
                                        },
                                        actions = {
                                            Surface(
                                                color = GoldAccent.copy(alpha = 0.25f),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.padding(end = 12.dp)
                                            ) {
                                                Text(
                                                    text = currentUser?.role ?: "",
                                                    color = GoldLight,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                        },
                                        colors = TopAppBarDefaults.topAppBarColors(
                                            containerColor = NavyPrimary,
                                            titleContentColor = White
                                        )
                                    )
                                },
                                contentWindowInsets = WindowInsets.safeDrawing
                            ) { paddingValues ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(paddingValues)
                                ) {
                                    when (currentScreen) {
                                        AppScreen.DASHBOARD -> DashboardScreen(
                                            stats = dashboardStats,
                                            onNavigate = { viewModel.navigateTo(it) }
                                        )
                                        AppScreen.PUNCH -> PunchScreen(
                                            employees = employees,
                                            shifts = shifts,
                                            onPunch = { empId, date, time, shiftId, isIn, callback ->
                                                viewModel.punch(empId, date, time, shiftId, isIn, callback)
                                            }
                                        )
                                        AppScreen.DAILY_LOG -> DailyLogScreen(
                                            currentDate = currentLogDate,
                                            rows = dailyRows,
                                            onDateChange = { viewModel.setLogDate(it) },
                                            onRefresh = { viewModel.loadDailyLog() },
                                            onEditRecord = { row, checkIn, checkOut, notes ->
                                                viewModel.loadDailyLog()
                                            }
                                        )
                                        AppScreen.EMPLOYEES -> EmployeesScreen(
                                            employees = employees,
                                            departments = departments,
                                            shifts = shifts,
                                            onSaveEmployee = { id, code, name, phone, dId, sId, hire, st, cb ->
                                                viewModel.saveEmployee(id, code, name, phone, dId, sId, hire, st, cb)
                                            },
                                            onDeleteEmployee = { emp, cb ->
                                                viewModel.deleteEmployee(emp, cb)
                                            }
                                        )
                                        AppScreen.DEPARTMENTS -> DepartmentsScreen(
                                            departments = departments,
                                            employees = employees,
                                            onSaveDepartment = { id, name, desc, cb ->
                                                viewModel.saveDepartment(id, name, desc, cb)
                                            },
                                            onDeleteDepartment = { dept, cb ->
                                                viewModel.deleteDepartment(dept, cb)
                                            }
                                        )
                                        AppScreen.SHIFTS -> ShiftsScreen(
                                            shifts = shifts,
                                            onSaveShift = { id, name, start, end, grace, cb ->
                                                viewModel.saveShift(id, name, start, end, grace, cb)
                                            },
                                            onDeleteShift = { shift, cb ->
                                                viewModel.deleteShift(shift, cb)
                                            }
                                        )
                                        AppScreen.LEAVES -> LeavesScreen(
                                            leaves = leaves,
                                            employees = employees,
                                            onSaveLeave = { id, empId, start, end, type, reason, st, cb ->
                                                viewModel.saveLeave(id, empId, start, end, type, reason, st, cb)
                                            },
                                            onDeleteLeave = { leave, cb ->
                                                viewModel.deleteLeave(leave, cb)
                                            }
                                        )
                                        AppScreen.REPORTS -> ReportsScreen(
                                            currentMonth = currentReportMonth,
                                            reportRows = reportRows,
                                            onMonthChange = { viewModel.setReportMonth(it) }
                                        )
                                        AppScreen.CALENDAR -> WorkdaysScreen(
                                            workdays = workdays,
                                            holidays = holidays,
                                            onSaveWorkdays = { list, cb ->
                                                viewModel.saveWorkdays(list, cb)
                                            },
                                            onAddHoliday = { date, name, notes, cb ->
                                                viewModel.addHoliday(date, name, notes, cb)
                                            },
                                            onDeleteHoliday = { h, cb ->
                                                viewModel.deleteHoliday(h, cb)
                                            }
                                        )
                                        AppScreen.USERS -> UsersScreen(
                                            users = users,
                                            currentLoggedInUser = currentUser,
                                            onSaveUser = { id, u, f, r, act, p, cb ->
                                                viewModel.saveUser(id, u, f, r, act, p, cb)
                                            },
                                            onDeactivateUser = { u, cb ->
                                                viewModel.deactivateUser(u, cb)
                                            }
                                        )
                                        AppScreen.AUDIT -> AuditLogScreen(logs = auditLogs)
                                        AppScreen.BACKUP -> BackupScreen()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getScreenIcon(screen: AppScreen): ImageVector {
        return when (screen) {
            AppScreen.DASHBOARD -> Icons.Default.Dashboard
            AppScreen.PUNCH -> Icons.Default.AccessTime
            AppScreen.DAILY_LOG -> Icons.Default.ListAlt
            AppScreen.EMPLOYEES -> Icons.Default.People
            AppScreen.DEPARTMENTS -> Icons.Default.Business
            AppScreen.SHIFTS -> Icons.Default.Schedule
            AppScreen.LEAVES -> Icons.Default.DateRange
            AppScreen.REPORTS -> Icons.Default.BarChart
            AppScreen.CALENDAR -> Icons.Default.CalendarToday
            AppScreen.USERS -> Icons.Default.AdminPanelSettings
            AppScreen.AUDIT -> Icons.Default.History
            AppScreen.BACKUP -> Icons.Default.Backup
        }
    }
}
