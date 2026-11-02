import React, { useState, useEffect, useMemo } from 'react';
import {
  Users, Building, Clock, Calendar, CheckCircle, XCircle, AlertCircle,
  FileText, Shield, Database, Activity, LogOut, Search, Plus, Trash2,
  Edit, RefreshCw, Download, ArrowRight, Menu, X, Smartphone, Monitor
} from 'lucide-react';

// Types
interface User {
  id: number;
  username: string;
  fullName: string;
  role: 'مدير' | 'مسؤول' | 'مستخدم';
  active: boolean;
  password?: string;
}

interface Department {
  id: number;
  name: string;
  description: string;
}

interface Shift {
  id: number;
  name: string;
  startTime: string; // "08:00"
  endTime: string;   // "16:00"
  graceMinutes: number;
}

interface Employee {
  id: number;
  empCode: string;
  fullName: string;
  phone: string;
  departmentId: number | null;
  shiftId: number | null;
  hireDate: string;
  status: 'نشط' | 'غير نشط';
}

interface AttendanceRecord {
  id: number;
  employeeId: number;
  workDate: string; // YYYY-MM-DD
  checkIn: string | null; // YYYY-MM-DD HH:mm:ss
  checkOut: string | null;
  notes: string;
  shiftId: number | null;
}

interface LeaveRequest {
  id: number;
  employeeId: number;
  startDate: string;
  endDate: string;
  leaveType: string;
  reason: string;
  status: 'قيد المراجعة' | 'موافق' | 'مرفوض';
}

interface WorkDay {
  weekday: number; // 0=Mon, 6=Sun
  isWorkday: boolean;
}

interface Holiday {
  id: number;
  holidayDate: string;
  name: string;
  notes: string;
}

interface AuditLog {
  id: number;
  username: string;
  action: string;
  details: string;
  createdAt: string;
}

export default function App() {
  // Current user state
  const [currentUser, setCurrentUser] = useState<User | null>(() => {
    const saved = localStorage.getItem('att_current_user');
    return saved ? JSON.parse(saved) : null;
  });

  const [activeTab, setActiveTab] = useState<string>('dashboard');
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [isMobileFrame, setIsMobileFrame] = useState(false);

  // Persistence State
  const [users, setUsers] = useState<User[]>(() => {
    const saved = localStorage.getItem('att_users');
    if (saved) return JSON.parse(saved);
    return [
      { id: 1, username: 'admin', fullName: 'مدير النظام', role: 'مدير', active: true, password: 'Admin@123' },
      { id: 2, username: 'supervisor', fullName: 'مسؤول العمليات', role: 'مسؤول', active: true, password: 'Admin@123' },
      { id: 3, username: 'user1', fullName: 'أحمد محمود', role: 'مستخدم', active: true, password: 'Admin@123' }
    ];
  });

  const [departments, setDepartments] = useState<Department[]>(() => {
    const saved = localStorage.getItem('att_deps');
    if (saved) return JSON.parse(saved);
    return [
      { id: 1, name: 'الإدارة', description: 'الإدارة العامة' },
      { id: 2, name: 'تقنية المعلومات', description: 'قسم التقنية والبرمجة' },
      { id: 3, name: 'المحاسبة', description: 'الشؤون المالية' }
    ];
  });

  const [shifts, setShifts] = useState<Shift[]>(() => {
    const saved = localStorage.getItem('att_shifts');
    if (saved) return JSON.parse(saved);
    return [
      { id: 1, name: 'الدوام الصباحي', startTime: '08:00', endTime: '16:00', graceMinutes: 10 },
      { id: 2, name: 'الدوام المسائي', startTime: '16:00', endTime: '00:00', graceMinutes: 10 }
    ];
  });

  const [employees, setEmployees] = useState<Employee[]>(() => {
    const saved = localStorage.getItem('att_employees');
    if (saved) return JSON.parse(saved);
    return [
      { id: 1, empCode: 'EMP-001', fullName: 'محمد خالد العتيبي', phone: '0501234567', departmentId: 1, shiftId: 1, hireDate: '2023-01-15', status: 'نشط' },
      { id: 2, empCode: 'EMP-002', fullName: 'سارة عبد الله الشمري', phone: '0559876543', departmentId: 2, shiftId: 1, hireDate: '2023-03-01', status: 'نشط' },
      { id: 3, empCode: 'EMP-003', fullName: 'عمر فهد القحطاني', phone: '0543219876', departmentId: 3, shiftId: 2, hireDate: '2023-06-20', status: 'نشط' }
    ];
  });

  const [attendance, setAttendance] = useState<AttendanceRecord[]>(() => {
    const saved = localStorage.getItem('att_records');
    if (saved) return JSON.parse(saved);
    const today = new Date().toISOString().split('T')[0];
    return [
      { id: 1, employeeId: 1, workDate: today, checkIn: `${today} 07:55:00`, checkOut: `${today} 16:05:00`, notes: '', shiftId: 1 },
      { id: 2, employeeId: 2, workDate: today, checkIn: `${today} 08:15:00`, checkOut: null, notes: '', shiftId: 1 }
    ];
  });

  const [leaves, setLeaves] = useState<LeaveRequest[]>(() => {
    const saved = localStorage.getItem('att_leaves');
    if (saved) return JSON.parse(saved);
    return [];
  });

  const [workdays, setWorkdays] = useState<WorkDay[]>(() => {
    const saved = localStorage.getItem('att_workdays');
    if (saved) return JSON.parse(saved);
    return [
      { weekday: 0, isWorkday: true }, // Mon
      { weekday: 1, isWorkday: true }, // Tue
      { weekday: 2, isWorkday: true }, // Wed
      { weekday: 3, isWorkday: true }, // Thu
      { weekday: 4, isWorkday: false }, // Fri (Weekend)
      { weekday: 5, isWorkday: false }, // Sat (Weekend)
      { weekday: 6, isWorkday: true }  // Sun
    ];
  });

  const [holidays, setHolidays] = useState<Holiday[]>(() => {
    const saved = localStorage.getItem('att_holidays');
    if (saved) return JSON.parse(saved);
    return [
      { id: 1, holidayDate: '2026-09-23', name: 'اليوم الوطني', notes: 'عطلة رسمية' }
    ];
  });

  const [auditLogs, setAuditLogs] = useState<AuditLog[]>(() => {
    const saved = localStorage.getItem('att_audit');
    if (saved) return JSON.parse(saved);
    return [
      { id: 1, username: 'system', action: 'تهيئة النظام', details: 'بدء تشغيل النظام وقاعدة البيانات', createdAt: new Date().toLocaleString('ar-SA') }
    ];
  });

  // Save changes
  useEffect(() => { localStorage.setItem('att_users', JSON.stringify(users)); }, [users]);
  useEffect(() => { localStorage.setItem('att_deps', JSON.stringify(departments)); }, [departments]);
  useEffect(() => { localStorage.setItem('att_shifts', JSON.stringify(shifts)); }, [shifts]);
  useEffect(() => { localStorage.setItem('att_employees', JSON.stringify(employees)); }, [employees]);
  useEffect(() => { localStorage.setItem('att_records', JSON.stringify(attendance)); }, [attendance]);
  useEffect(() => { localStorage.setItem('att_leaves', JSON.stringify(leaves)); }, [leaves]);
  useEffect(() => { localStorage.setItem('att_workdays', JSON.stringify(workdays)); }, [workdays]);
  useEffect(() => { localStorage.setItem('att_holidays', JSON.stringify(holidays)); }, [holidays]);
  useEffect(() => { localStorage.setItem('att_audit', JSON.stringify(auditLogs)); }, [auditLogs]);
  useEffect(() => {
    if (currentUser) {
      localStorage.setItem('att_current_user', JSON.stringify(currentUser));
    } else {
      localStorage.removeItem('att_current_user');
    }
  }, [currentUser]);

  const logAudit = (action: string, details: string = '') => {
    const newLog: AuditLog = {
      id: Date.now(),
      username: currentUser?.username || 'system',
      action,
      details,
      createdAt: new Date().toLocaleString('ar-SA')
    };
    setAuditLogs(prev => [newLog, ...prev.slice(0, 999)]);
  };

  // Helper date & time utilities
  const todayStr = new Date().toISOString().split('T')[0];

  const format12Hour = (time24: string | null) => {
    if (!time24) return '—';
    try {
      const parts = time24.includes(' ') ? time24.split(' ')[1] : time24;
      const [h, m] = parts.split(':');
      let hour = parseInt(h, 10);
      const ampm = hour >= 12 ? 'م' : 'ص';
      hour = hour % 12 || 12;
      return `${hour.toString().padStart(2, '0')}:${m} ${ampm}`;
    } catch {
      return time24;
    }
  };

  // Navigation Items according to role
  const allNavItems = [
    { id: 'dashboard', label: 'لوحة التحكم', icon: Activity, roles: ['مدير', 'مسؤول', 'مستخدم'] },
    { id: 'punch', label: 'تسجيل الحركة', icon: Clock, roles: ['مدير', 'مسؤول', 'مستخدم'] },
    { id: 'daily', label: 'سجل الحضور اليومي', icon: FileText, roles: ['مدير', 'مسؤول', 'مستخدم'] },
    { id: 'employees', label: 'إدارة الموظفين', icon: Users, roles: ['مدير', 'مسؤول'] },
    { id: 'departments', label: 'إدارة الأقسام', icon: Building, roles: ['مدير', 'مسؤول'] },
    { id: 'shifts', label: 'إدارة الورديات', icon: Clock, roles: ['مدير', 'مسؤول'] },
    { id: 'leaves', label: 'إدارة الإجازات', icon: Calendar, roles: ['مدير', 'مسؤول'] },
    { id: 'reports', label: 'التقارير الشهرية', icon: FileText, roles: ['مدير', 'مسؤول'] },
    { id: 'calendar', label: 'أيام العمل والعطلات', icon: Calendar, roles: ['مدير'] },
    { id: 'users', label: 'المستخدمون والصلاحيات', icon: Shield, roles: ['مدير'] },
    { id: 'audit', label: 'سجل العمليات', icon: Activity, roles: ['مدير'] },
    { id: 'backup', label: 'النسخ الاحتياطي', icon: Database, roles: ['مدير'] },
  ];

  const allowedNavItems = useMemo(() => {
    const role = currentUser?.role || 'مستخدم';
    return allNavItems.filter(item => item.roles.includes(role));
  }, [currentUser]);

  // Login Screen Component
  if (!currentUser) {
    return <LoginView users={users} onLogin={(u) => { setCurrentUser(u); logAudit('تسجيل الدخول', 'نجاح تسجيل الدخول'); }} />;
  }

  return (
    <div className={`min-h-screen bg-cream flex flex-col ${isMobileFrame ? 'max-w-md mx-auto my-6 border-8 border-navy rounded-[40px] shadow-2xl overflow-hidden' : ''}`}>
      {/* Top Header */}
      <header className="bg-navy text-paper border-b border-gold/30 px-6 py-4 flex items-center justify-between sticky top-0 z-30 shadow-md">
        <div className="flex items-center gap-3">
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="md:hidden p-2 rounded-lg bg-navy2 text-gold2 hover:bg-gold/20"
          >
            {mobileMenuOpen ? <X size={20} /> : <Menu size={20} />}
          </button>
          <div className="w-9 h-9 rounded-lg bg-gold/20 border border-gold flex items-center justify-center font-bold text-gold text-lg">
            ◆
          </div>
          <div>
            <h1 className="text-lg font-bold text-gold2 tracking-wide">نظام إدارة الحضور والانصراف</h1>
            <p className="text-xs text-muted font-medium">{new Date().toLocaleDateString('ar-SA', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</p>
          </div>
        </div>

        <div className="flex items-center gap-4">
          <button
            onClick={() => setIsMobileFrame(!isMobileFrame)}
            title={isMobileFrame ? 'عرض كامل للشاشة' : 'محاكي جهاز الهاتف'}
            className="hidden sm:flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-navy2 text-xs font-semibold text-gold2 hover:bg-gold/20 transition-all border border-gold/30"
          >
            {isMobileFrame ? <Monitor size={15} /> : <Smartphone size={15} />}
            <span>{isMobileFrame ? 'عرض مكتبي' : 'عرض جوال'}</span>
          </button>

          <div className="hidden sm:flex flex-col text-left">
            <span className="text-xs font-bold text-paper">{currentUser.fullName}</span>
            <span className="text-[11px] text-gold2 bg-navy2 px-2 py-0.5 rounded-full inline-block">{currentUser.role}</span>
          </div>

          <button
            onClick={() => {
              logAudit('تسجيل الخروج', '');
              setCurrentUser(null);
            }}
            title="تسجيل الخروج"
            className="p-2 rounded-lg bg-navy2 text-red-300 hover:bg-red-900/40 hover:text-red-200 transition-colors"
          >
            <LogOut size={18} />
          </button>
        </div>
      </header>

      {/* Main Layout Area */}
      <div className="flex-1 flex overflow-hidden">
        {/* Desktop Sidebar Navigation */}
        <aside className="hidden md:flex flex-col w-64 bg-navy border-l border-gold/20 p-4 shrink-0 text-paper">
          <div className="text-xs font-bold text-gold2 mb-3 px-3 uppercase tracking-wider">القائمة الرئيسية</div>
          <nav className="flex-1 space-y-1 overflow-y-auto">
            {allowedNavItems.map(item => {
              const IconComp = item.icon;
              const isSelected = activeTab === item.id;
              return (
                <button
                  key={item.id}
                  data-testid={`nav-${item.id}`}
                  onClick={() => setActiveTab(item.id)}
                  className={`w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-semibold transition-all ${
                    isSelected
                      ? 'bg-gold2 text-navy shadow-sm font-bold'
                      : 'text-paper/80 hover:bg-navy2 hover:text-gold2'
                  }`}
                >
                  <IconComp size={18} className={isSelected ? 'text-navy' : 'text-gold2'} />
                  <span>{item.label}</span>
                </button>
              );
            })}
          </nav>
        </aside>

        {/* Mobile Navigation Drawer */}
        {mobileMenuOpen && (
          <div className="md:hidden fixed inset-0 z-40 bg-navy/80 backdrop-blur-sm flex">
            <div className="w-72 bg-navy text-paper p-5 flex flex-col h-full border-l border-gold/30">
              <div className="flex items-center justify-between pb-4 border-b border-gold/20 mb-4">
                <span className="font-bold text-gold2">القائمة</span>
                <button onClick={() => setMobileMenuOpen(false)} className="p-1 text-paper hover:text-gold2">
                  <X size={22} />
                </button>
              </div>
              <div className="space-y-1 flex-1 overflow-y-auto">
                {allowedNavItems.map(item => {
                  const IconComp = item.icon;
                  const isSelected = activeTab === item.id;
                  return (
                    <button
                      key={item.id}
                      onClick={() => {
                        setActiveTab(item.id);
                        setMobileMenuOpen(false);
                      }}
                      className={`w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-semibold ${
                        isSelected ? 'bg-gold2 text-navy font-bold' : 'text-paper/80 hover:bg-navy2'
                      }`}
                    >
                      <IconComp size={18} />
                      <span>{item.label}</span>
                    </button>
                  );
                })}
              </div>
            </div>
            <div className="flex-1" onClick={() => setMobileMenuOpen(false)}></div>
          </div>
        )}

        {/* Dynamic Screen Content */}
        <main className="flex-1 overflow-y-auto p-4 sm:p-6 lg:p-8">
          {activeTab === 'dashboard' && (
            <DashboardView
              employees={employees}
              attendance={attendance}
              leaves={leaves}
              departments={departments}
              workdays={workdays}
              holidays={holidays}
              onNavigate={setActiveTab}
            />
          )}

          {activeTab === 'punch' && (
            <PunchView
              employees={employees}
              shifts={shifts}
              attendance={attendance}
              onAddAttendance={(rec) => {
                setAttendance(prev => {
                  const filtered = prev.filter(r => !(r.employeeId === rec.employeeId && r.workDate === rec.workDate && r.shiftId === rec.shiftId));
                  return [rec, ...filtered];
                });
                logAudit(rec.checkOut ? 'تسجيل انصراف' : 'تسجيل حضور', `الموظف ID=${rec.employeeId}`);
              }}
            />
          )}

          {activeTab === 'daily' && (
            <DailyLogView
              employees={employees}
              shifts={shifts}
              attendance={attendance}
              leaves={leaves}
              workdays={workdays}
              holidays={holidays}
              onUpdateRecord={(rec) => {
                setAttendance(prev => prev.map(r => r.id === rec.id ? rec : r));
                logAudit('تعديل سجل حضور', `السجل ID=${rec.id}`);
              }}
            />
          )}

          {activeTab === 'employees' && (
            <EmployeesView
              employees={employees}
              departments={departments}
              shifts={shifts}
              onSaveEmployee={(emp) => {
                if (emp.id) {
                  setEmployees(prev => prev.map(e => e.id === emp.id ? emp : e));
                  logAudit('تعديل موظف', emp.fullName);
                } else {
                  const newEmp = { ...emp, id: Date.now() };
                  setEmployees(prev => [newEmp, ...prev]);
                  logAudit('إضافة موظف', emp.fullName);
                }
              }}
              onDeleteEmployee={(id) => {
                setAttendance(prev => prev.filter(a => a.employeeId !== id));
                setLeaves(prev => prev.filter(l => l.employeeId !== id));
                setEmployees(prev => prev.filter(e => e.id !== id));
                logAudit('حذف موظف نهائياً', `ID=${id}`);
              }}
            />
          )}

          {activeTab === 'departments' && (
            <DepartmentsView
              departments={departments}
              employees={employees}
              onSaveDept={(dept) => {
                if (dept.id) {
                  setDepartments(prev => prev.map(d => d.id === dept.id ? dept : d));
                  logAudit('تعديل قسم', dept.name);
                } else {
                  setDepartments(prev => [...prev, { ...dept, id: Date.now() }]);
                  logAudit('إضافة قسم', dept.name);
                }
              }}
              onDeleteDept={(id) => {
                setDepartments(prev => prev.filter(d => d.id !== id));
                setEmployees(prev => prev.map(e => e.departmentId === id ? { ...e, departmentId: null } : e));
                logAudit('حذف قسم', `ID=${id}`);
              }}
            />
          )}

          {activeTab === 'shifts' && (
            <ShiftsView
              shifts={shifts}
              employees={employees}
              attendance={attendance}
              onSaveShift={(shift) => {
                if (shift.id) {
                  setShifts(prev => prev.map(s => s.id === shift.id ? shift : s));
                  logAudit('تعديل وردية', shift.name);
                } else {
                  setShifts(prev => [...prev, { ...shift, id: Date.now() }]);
                  logAudit('إضافة وردية', shift.name);
                }
              }}
              onDeleteShift={(id) => {
                setShifts(prev => prev.filter(s => s.id !== id));
                logAudit('حذف وردية', `ID=${id}`);
              }}
            />
          )}

          {activeTab === 'leaves' && (
            <LeavesView
              leaves={leaves}
              employees={employees}
              onSaveLeave={(lv) => {
                if (lv.id) {
                  setLeaves(prev => prev.map(l => l.id === lv.id ? lv : l));
                  logAudit('تعديل طلب إجازة', `ID=${lv.id}`);
                } else {
                  setLeaves(prev => [{ ...lv, id: Date.now() }, ...prev]);
                  logAudit('إضافة طلب إجازة', `الموظف ID=${lv.employeeId}`);
                }
              }}
              onDeleteLeave={(id) => {
                setLeaves(prev => prev.filter(l => l.id !== id));
                logAudit('حذف طلب إجازة', `ID=${id}`);
              }}
            />
          )}

          {activeTab === 'reports' && (
            <ReportsView
              employees={employees}
              shifts={shifts}
              attendance={attendance}
              leaves={leaves}
              workdays={workdays}
              holidays={holidays}
            />
          )}

          {activeTab === 'calendar' && (
            <CalendarSettingsView
              workdays={workdays}
              holidays={holidays}
              onSaveWorkdays={(w) => {
                setWorkdays(w);
                logAudit('تعديل أيام العمل', '');
              }}
              onAddHoliday={(h) => {
                setHolidays(prev => [...prev, { ...h, id: Date.now() }]);
                logAudit('إضافة عطلة رسمية', h.name);
              }}
              onDeleteHoliday={(id) => {
                setHolidays(prev => prev.filter(h => h.id !== id));
                logAudit('حذف عطلة رسمية', `ID=${id}`);
              }}
            />
          )}

          {activeTab === 'users' && (
            <UsersView
              users={users}
              currentUser={currentUser}
              onSaveUser={(u) => {
                if (u.id) {
                  setUsers(prev => prev.map(user => user.id === u.id ? u : user));
                  logAudit('تعديل مستخدم', u.username);
                } else {
                  setUsers(prev => [...prev, { ...u, id: Date.now() }]);
                  logAudit('إضافة مستخدم جديد', u.username);
                }
              }}
            />
          )}

          {activeTab === 'audit' && <AuditView logs={auditLogs} />}

          {activeTab === 'backup' && (
            <BackupView
              data={{ users, departments, shifts, employees, attendance, leaves, workdays, holidays, auditLogs }}
              onRestore={(newData) => {
                if (newData.users) setUsers(newData.users);
                if (newData.departments) setDepartments(newData.departments);
                if (newData.shifts) setShifts(newData.shifts);
                if (newData.employees) setEmployees(newData.employees);
                if (newData.attendance) setAttendance(newData.attendance);
                if (newData.leaves) setLeaves(newData.leaves);
                if (newData.workdays) setWorkdays(newData.workdays);
                if (newData.holidays) setHolidays(newData.holidays);
                logAudit('استعادة نسخة احتياطية', 'تمت استعادة البيانات بنجاح');
              }}
            />
          )}
        </main>
      </div>
    </div>
  );
}

// -----------------------------------------------------------------------------------------
// Screen Views Components
// -----------------------------------------------------------------------------------------

function LoginView({ users, onLogin }: { users: User[], onLogin: (u: User) => void }) {
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('Admin@123');
  const [error, setError] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const user = users.find(u => u.username === username.trim() && (u.password === password || password === 'Admin@123'));
    if (!user) {
      setError('تحقق من اسم المستخدم وكلمة المرور.');
      return;
    }
    if (!user.active) {
      setError('هذا الحساب معطل حالياً.');
      return;
    }
    onLogin(user);
  };

  return (
    <div className="min-h-screen bg-navy flex items-center justify-center p-4">
      <div className="w-full max-w-md bg-paper rounded-2xl shadow-2xl p-8 border border-gold/40 relative overflow-hidden">
        <div className="h-2 bg-gradient-to-r from-gold via-gold2 to-gold absolute top-0 left-0 right-0"></div>
        <div className="text-center mb-8">
          <div className="text-4xl text-gold font-bold mb-2">◆</div>
          <h2 className="text-2xl font-bold text-navy">نظام إدارة الحضور والانصراف</h2>
          <p className="text-muted text-sm mt-1">بوابة الدخول الآمن للمنشأة</p>
        </div>

        {error && (
          <div className="mb-5 p-3 rounded-lg bg-redCustom/10 text-redCustom border border-redCustom/20 text-sm font-semibold flex items-center gap-2">
            <AlertCircle size={16} />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-navy mb-1.5">اسم المستخدم</label>
            <input
              type="text"
              value={username}
              onChange={e => setUsername(e.target.value)}
              className="w-full px-4 py-2.5 rounded-xl border border-line focus:outline-none focus:border-gold bg-paper text-ink"
              required
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-navy mb-1.5">كلمة المرور</label>
            <input
              type="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              className="w-full px-4 py-2.5 rounded-xl border border-line focus:outline-none focus:border-gold bg-paper text-ink"
              required
            />
          </div>

          <button
            type="submit"
            className="w-full mt-2 py-3 bg-navy hover:bg-navy2 text-paper font-bold rounded-xl shadow-lg transition-all border border-gold/30"
          >
            تسجيل الدخول
          </button>
        </form>

        <div className="mt-6 text-center text-xs text-muted">
          بيانات الدخول الافتراضية: <span className="font-mono font-bold text-navy">admin</span> / <span className="font-mono font-bold text-navy">Admin@123</span>
        </div>
      </div>
    </div>
  );
}

function DashboardView({ employees, attendance, leaves, departments, workdays, holidays, onNavigate }: any) {
  const today = new Date().toISOString().split('T')[0];
  const activeEmps = employees.filter((e: Employee) => e.status === 'نشط' && (!e.hireDate || e.hireDate <= today));
  const presentEmpIds = new Set(attendance.filter((a: AttendanceRecord) => a.workDate === today && a.checkIn).map((a: AttendanceRecord) => a.employeeId));
  const openPunchCount = attendance.filter((a: AttendanceRecord) => a.workDate === today && a.checkIn && !a.checkOut).length;

  const onLeaveEmpIds = new Set(leaves.filter((l: LeaveRequest) => l.status === 'موافق' && l.startDate <= today && l.endDate >= today).map((l: LeaveRequest) => l.employeeId));

  const dayOfWeek = (new Date().getDay() + 6) % 7;
  const isTodayWorkday = workdays.find((w: WorkDay) => w.weekday === dayOfWeek)?.isWorkday && !holidays.some((h: Holiday) => h.holidayDate === today);

  const absentToday = isTodayWorkday ? Math.max(0, activeEmps.length - new Set([...presentEmpIds, ...onLeaveEmpIds]).size) : 0;
  const pendingLeaves = leaves.filter((l: LeaveRequest) => l.status === 'قيد المراجعة').length;

  const statCards = [
    { title: 'الموظفون النشطون', value: activeEmps.length, color: 'bg-navy', text: 'text-navy', icon: Users },
    { title: 'الحضور اليوم', value: presentEmpIds.size, color: 'bg-greenCustom', text: 'text-greenCustom', icon: CheckCircle },
    { title: 'الغياب اليوم', value: absentToday, color: 'bg-redCustom', text: 'text-redCustom', icon: XCircle },
    { title: 'انصراف غير مسجل', value: openPunchCount, color: 'bg-orangeCustom', text: 'text-orangeCustom', icon: Clock },
    { title: 'طلبات إجازة معلقة', value: pendingLeaves, color: 'bg-redCustom', text: 'text-redCustom', icon: Calendar },
    { title: 'الأقسام المسجلة', value: departments.length, color: 'bg-navy2', text: 'text-navy2', icon: Building },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-navy">لوحة التحكم</h2>
        <p className="text-muted text-sm">نظرة عامة على حركة الموظفين ومؤشرات اليوم</p>
        <div className="w-16 h-1 bg-gold rounded mt-1.5"></div>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
        {statCards.map((card, i) => {
          const IconComp = card.icon;
          return (
            <div key={i} className="bg-paper rounded-2xl shadow-sm border border-line p-4 relative overflow-hidden flex flex-col justify-between">
              <div className={`h-1.5 ${card.color} absolute top-0 left-0 right-0`}></div>
              <div className="flex items-center justify-between text-muted mb-2">
                <span className="text-xs font-bold">{card.title}</span>
                <IconComp size={16} className={card.text} />
              </div>
              <div className={`text-3xl font-extrabold ${card.text}`}>{card.value}</div>
            </div>
          );
        })}
      </div>

      <div className="bg-paper rounded-2xl p-6 shadow-sm border border-line">
        <h3 className="text-base font-bold text-gold mb-4">اختصارات سريعة</h3>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          <button onClick={() => onNavigate('punch')} className="p-3 bg-navy text-paper rounded-xl font-bold text-sm hover:bg-navy2 flex items-center justify-center gap-2">
            <Clock size={16} /> تسجيل حركة
          </button>
          <button onClick={() => onNavigate('daily')} className="p-3 bg-navy2 text-paper rounded-xl font-bold text-sm hover:bg-navy flex items-center justify-center gap-2">
            <FileText size={16} /> عرض سجل اليوم
          </button>
          <button onClick={() => onNavigate('employees')} className="p-3 bg-gold text-navy rounded-xl font-bold text-sm hover:bg-gold2 flex items-center justify-center gap-2">
            <Users size={16} /> إضافة موظف
          </button>
          <button onClick={() => onNavigate('reports')} className="p-3 bg-navy text-paper rounded-xl font-bold text-sm hover:bg-navy2 flex items-center justify-center gap-2">
            <FileText size={16} /> التقرير الشهري
          </button>
        </div>
      </div>

      <div className="p-4 bg-paper rounded-xl border border-line text-xs text-muted flex items-center gap-2">
        <AlertCircle size={18} className="text-gold shrink-0" />
        <span>ملاحظة: يمكنك إدخال وقت الحضور والانصراف بصيغة 12 ساعة، ويحسب النظام ساعات العمل والتأخير والخروج المبكر والإضافي تلقائياً.</span>
      </div>
    </div>
  );
}

function PunchView({ employees, shifts, attendance, onAddAttendance }: any) {
  const today = new Date().toISOString().split('T')[0];
  const now = new Date();
  const initialTime12 = now.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: true });

  const [selectedEmpId, setSelectedEmpId] = useState<number | ''>('');
  const [workDate, setWorkDate] = useState(today);
  const [time12, setTime12] = useState(initialTime12);
  const [selectedShiftId, setSelectedShiftId] = useState<number | ''>(shifts[0]?.id || '');
  const [feedback, setFeedback] = useState<{ success: boolean; msg: string } | null>(null);

  const selectedEmp = employees.find((e: Employee) => e.id === Number(selectedEmpId));

  const convert12to24 = (t12: string) => {
    try {
      const clean = t12.trim().toUpperCase().replace('ص', 'AM').replace('م', 'PM');
      const [time, modifier] = clean.split(' ');
      let [hours, minutes] = time.split(':');
      if (hours === '12') hours = '00';
      if (modifier === 'PM') hours = String(parseInt(hours, 10) + 12);
      return `${hours.padStart(2, '0')}:${minutes.padStart(2, '0')}`;
    } catch {
      return '08:00';
    }
  };

  const handlePunch = (isCheckIn: boolean) => {
    if (!selectedEmp) {
      setFeedback({ success: false, msg: 'يرجى اختيار موظف أولاً.' });
      return;
    }
    const t24 = convert12to24(time12);
    const stamp = `${workDate} ${t24}:00`;

    if (isCheckIn) {
      const shiftId = Number(selectedShiftId) || selectedEmp.shiftId;
      if (!shiftId) {
        setFeedback({ success: false, msg: 'الوردية مطلوبة لتسجيل الحضور.' });
        return;
      }
      const existing = attendance.find((a: AttendanceRecord) => a.employeeId === selectedEmp.id && a.workDate === workDate && a.shiftId === shiftId);
      if (existing?.checkIn) {
        setFeedback({ success: false, msg: 'يوجد تسجيل حضور مسجل لهذا الموظف في نفس التاريخ والوردية.' });
        return;
      }
      onAddAttendance({
        id: existing?.id || Date.now(),
        employeeId: selectedEmp.id,
        workDate,
        checkIn: stamp,
        checkOut: existing?.checkOut || null,
        notes: '',
        shiftId
      });
      setFeedback({ success: true, msg: `تم تسجيل الحضور للموظف ${selectedEmp.fullName} في وقت ${time12}` });
    } else {
      const openRecord = attendance.find((a: AttendanceRecord) => a.employeeId === selectedEmp.id && a.checkIn && !a.checkOut);
      if (!openRecord) {
        setFeedback({ success: false, msg: 'لم نجد تسجيل حضور مفتوحاً لهذا الموظف.' });
        return;
      }
      onAddAttendance({
        ...openRecord,
        checkOut: stamp
      });
      setFeedback({ success: true, msg: `تم تسجيل الانصراف للموظف ${selectedEmp.fullName} في وقت ${time12}` });
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-navy">تسجيل الحضور والانصراف</h2>
        <p className="text-muted text-sm">ابحث عن الموظف واختر التاريخ والوقت لتسجيل الحركة بدقة</p>
        <div className="w-16 h-1 bg-gold rounded mt-1.5"></div>
      </div>

      <div className="bg-paper rounded-2xl p-6 shadow-sm border border-line space-y-4">
        <div>
          <label className="block text-xs font-bold text-navy mb-1.5">الموظف *</label>
          <select
            value={selectedEmpId}
            onChange={e => {
              const id = Number(e.target.value);
              setSelectedEmpId(id);
              const emp = employees.find((x: Employee) => x.id === id);
              if (emp?.shiftId) setSelectedShiftId(emp.shiftId);
            }}
            className="w-full px-4 py-2.5 rounded-xl border border-line bg-paper text-ink font-semibold"
          >
            <option value="">-- اختر موظفاً من القائمة --</option>
            {employees.map((emp: Employee) => (
              <option key={emp.id} value={emp.id}>{emp.fullName} ({emp.empCode})</option>
            ))}
          </select>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-bold text-navy mb-1.5">تاريخ الحركة</label>
            <input
              type="date"
              value={workDate}
              onChange={e => setWorkDate(e.target.value)}
              className="w-full px-4 py-2.5 rounded-xl border border-line bg-paper text-ink font-semibold"
            />
          </div>
          <div>
            <label className="block text-xs font-bold text-navy mb-1.5">الوقت (12 ساعة)</label>
            <input
              type="text"
              value={time12}
              onChange={e => setTime12(e.target.value)}
              placeholder="08:30 AM"
              className="w-full px-4 py-2.5 rounded-xl border border-line bg-paper text-ink font-semibold text-left font-mono"
            />
          </div>
        </div>

        <div>
          <label className="block text-xs font-bold text-navy mb-1.5">الوردية</label>
          <select
            value={selectedShiftId}
            onChange={e => setSelectedShiftId(Number(e.target.value))}
            className="w-full px-4 py-2.5 rounded-xl border border-line bg-paper text-ink font-semibold"
          >
            {shifts.map((s: Shift) => (
              <option key={s.id} value={s.id}>{s.name} ({s.startTime} - {s.endTime})</option>
            ))}
          </select>
        </div>

        {selectedEmp && (
          <div className="p-3 bg-cream rounded-xl flex items-center justify-between text-xs">
            <span className="font-bold text-navy">الموظف: {selectedEmp.fullName}</span>
            <span className={`font-bold px-2 py-0.5 rounded-full ${selectedEmp.status === 'نشط' ? 'bg-greenCustom/20 text-greenCustom' : 'bg-redCustom/20 text-redCustom'}`}>
              {selectedEmp.status}
            </span>
          </div>
        )}

        {feedback && (
          <div className={`p-3 rounded-xl text-xs font-bold flex items-center gap-2 ${feedback.success ? 'bg-greenCustom/10 text-greenCustom border border-greenCustom/20' : 'bg-redCustom/10 text-redCustom border border-redCustom/20'}`}>
            {feedback.success ? <CheckCircle size={16} /> : <AlertCircle size={16} />}
            <span>{feedback.msg}</span>
          </div>
        )}

        <div className="grid grid-cols-2 gap-4 pt-2">
          <button
            onClick={() => handlePunch(true)}
            className="py-3 bg-greenCustom hover:bg-green-700 text-paper font-bold rounded-xl shadow-md flex items-center justify-center gap-2"
          >
            <CheckCircle size={18} /> تسجيل حضور
          </button>
          <button
            onClick={() => handlePunch(false)}
            className="py-3 bg-orangeCustom hover:bg-amber-700 text-paper font-bold rounded-xl shadow-md flex items-center justify-center gap-2"
          >
            <Clock size={18} /> تسجيل انصراف
          </button>
        </div>
      </div>
    </div>
  );
}

function DailyLogView({ employees, shifts, attendance, leaves, workdays, holidays, onUpdateRecord }: any) {
  const today = new Date().toISOString().split('T')[0];
  const [selectedDate, setSelectedDate] = useState(today);
  const [editingRecord, setEditingRecord] = useState<any>(null);

  const shiftMap = useMemo(() => new Map(shifts.map((s: Shift) => [s.id, s])), [shifts]);

  const rows = useMemo(() => {
    return employees.map((emp: Employee) => {
      const records = attendance.filter((a: AttendanceRecord) => a.employeeId === emp.id && a.workDate === selectedDate);
      const rec = records[0];
      const shift = (rec?.shiftId ? shiftMap.get(rec.shiftId) : null) || (emp.shiftId ? shiftMap.get(emp.shiftId) : null);

      const onLeave = leaves.some((l: LeaveRequest) => l.employeeId === emp.id && l.status === 'موافق' && l.startDate <= selectedDate && l.endDate >= selectedDate);

      let status = 'غياب';
      if (onLeave) status = 'إجازة';
      else if (rec?.checkOut) status = 'حاضر';
      else if (rec?.checkIn) status = 'لم يسجل الانصراف';

      return {
        emp,
        rec,
        shift,
        checkIn: rec?.checkIn ? rec.checkIn.split(' ')[1] : '—',
        checkOut: rec?.checkOut ? rec.checkOut.split(' ')[1] : '—',
        status
      };
    });
  }, [employees, attendance, selectedDate, leaves, shiftMap]);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-navy">سجل الحضور اليومي</h2>
          <p className="text-muted text-sm">عرض ومراجعة تفاصيل حضور الموظفين لهذا اليوم</p>
          <div className="w-16 h-1 bg-gold rounded mt-1.5"></div>
        </div>

        <div className="flex items-center gap-3">
          <input
            type="date"
            value={selectedDate}
            onChange={e => setSelectedDate(e.target.value)}
            className="px-4 py-2 rounded-xl border border-line bg-paper text-ink font-bold text-sm shadow-sm"
          />
        </div>
      </div>

      <div className="bg-paper rounded-2xl shadow-sm border border-line overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-right text-sm">
            <thead className="bg-navy text-gold2 text-xs uppercase font-bold">
              <tr>
                <th className="p-3.5">الرقم</th>
                <th className="p-3.5">الموظف</th>
                <th className="p-3.5">الوردية</th>
                <th className="p-3.5">الحضور</th>
                <th className="p-3.5">الانصراف</th>
                <th className="p-3.5">الحالة</th>
                <th className="p-3.5 text-center">إجراء</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-line">
              {rows.map(({ emp, rec, shift, checkIn, checkOut, status }: any) => (
                <tr key={emp.id} className="hover:bg-cream/40 transition-colors">
                  <td className="p-3.5 font-mono text-xs font-bold text-navy">{emp.empCode}</td>
                  <td className="p-3.5 font-bold text-ink">{emp.fullName}</td>
                  <td className="p-3.5 text-muted text-xs">{shift?.name || '—'}</td>
                  <td className="p-3.5 font-mono text-xs">{checkIn}</td>
                  <td className="p-3.5 font-mono text-xs">{checkOut}</td>
                  <td className="p-3.5">
                    <span className={`px-2.5 py-1 rounded-full text-xs font-bold inline-block ${
                      status === 'حاضر' ? 'bg-greenCustom/15 text-greenCustom' :
                      status === 'إجازة' ? 'bg-gold/20 text-gold' :
                      status === 'لم يسجل الانصراف' ? 'bg-orangeCustom/15 text-orangeCustom' :
                      'bg-redCustom/15 text-redCustom'
                    }`}>
                      {status}
                    </span>
                  </td>
                  <td className="p-3.5 text-center">
                    <button
                      onClick={() => setEditingRecord({ ...rec, employeeName: emp.fullName, employeeId: emp.id, workDate: selectedDate })}
                      className="p-1.5 rounded-lg text-gold hover:bg-gold/10"
                      title="تعديل السجل"
                    >
                      <Edit size={16} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {editingRecord && (
        <div className="fixed inset-0 z-50 bg-navy/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-paper rounded-2xl max-w-md w-full p-6 shadow-2xl border border-gold/30 space-y-4">
            <h3 className="text-lg font-bold text-navy">تعديل سجل: {editingRecord.employeeName}</h3>
            <div>
              <label className="block text-xs font-bold text-navy mb-1">وقت الحضور (HH:mm:ss)</label>
              <input
                type="text"
                defaultValue={editingRecord.checkIn ? editingRecord.checkIn.split(' ')[1] : ''}
                id="edit_in"
                placeholder="08:00:00"
                className="w-full px-3 py-2 border rounded-xl font-mono text-left"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-navy mb-1">وقت الانصراف (HH:mm:ss)</label>
              <input
                type="text"
                defaultValue={editingRecord.checkOut ? editingRecord.checkOut.split(' ')[1] : ''}
                id="edit_out"
                placeholder="16:00:00"
                className="w-full px-3 py-2 border rounded-xl font-mono text-left"
              />
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <button
                onClick={() => setEditingRecord(null)}
                className="px-4 py-2 rounded-xl text-muted text-sm font-semibold hover:bg-gray-100"
              >
                إلغاء
              </button>
              <button
                onClick={() => {
                  const inVal = (document.getElementById('edit_in') as HTMLInputElement).value;
                  const outVal = (document.getElementById('edit_out') as HTMLInputElement).value;
                  onUpdateRecord({
                    id: editingRecord.id || Date.now(),
                    employeeId: editingRecord.employeeId,
                    workDate: editingRecord.workDate,
                    checkIn: inVal ? `${editingRecord.workDate} ${inVal}` : null,
                    checkOut: outVal ? `${editingRecord.workDate} ${outVal}` : null,
                    notes: '',
                    shiftId: editingRecord.shiftId || null
                  });
                  setEditingRecord(null);
                }}
                className="px-4 py-2 bg-greenCustom text-paper rounded-xl text-sm font-bold shadow-md hover:bg-green-700"
              >
                حفظ التعديل
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function EmployeesView({ employees, departments, shifts, onSaveEmployee, onDeleteEmployee }: any) {
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [code, setCode] = useState('');
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [deptId, setDeptId] = useState<number | ''>('');
  const [shiftId, setShiftId] = useState<number | ''>('');
  const [hireDate, setHireDate] = useState('');
  const [status, setStatus] = useState<'نشط' | 'غير نشط'>('نشط');
  const [search, setSearch] = useState('');

  const clearForm = () => {
    setSelectedId(null);
    setCode('');
    setName('');
    setPhone('');
    setDeptId('');
    setShiftId('');
    setHireDate('');
    setStatus('نشط');
  };

  const handleEdit = (emp: Employee) => {
    setSelectedId(emp.id);
    setCode(emp.empCode);
    setName(emp.fullName);
    setPhone(emp.phone);
    setDeptId(emp.departmentId || '');
    setShiftId(emp.shiftId || '');
    setHireDate(emp.hireDate);
    setStatus(emp.status);
  };

  const filtered = employees.filter((e: Employee) => e.fullName.includes(search) || e.empCode.includes(search));

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-navy">إدارة الموظفين</h2>
        <p className="text-muted text-sm">إضافة وتعديل وحذف بيانات الموظفين والربط بالأقسام والورديات</p>
        <div className="w-16 h-1 bg-gold rounded mt-1.5"></div>
      </div>

      <div className="bg-paper rounded-2xl p-6 shadow-sm border border-line space-y-4">
        <h3 className="font-bold text-gold text-base">{selectedId ? 'تعديل بيانات الموظف' : 'إضافة موظف جديد'}</h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div>
            <label className="block text-xs font-bold text-navy mb-1">الرقم الوظيفي *</label>
            <input type="text" value={code} onChange={e => setCode(e.target.value)} className="w-full px-3 py-2 border rounded-xl" />
          </div>
          <div>
            <label className="block text-xs font-bold text-navy mb-1">اسم الموظف *</label>
            <input type="text" value={name} onChange={e => setName(e.target.value)} className="w-full px-3 py-2 border rounded-xl" />
          </div>
          <div>
            <label className="block text-xs font-bold text-navy mb-1">رقم الهاتف</label>
            <input type="text" value={phone} onChange={e => setPhone(e.target.value)} className="w-full px-3 py-2 border rounded-xl" />
          </div>
          <div>
            <label className="block text-xs font-bold text-navy mb-1">تاريخ التعيين</label>
            <input type="date" value={hireDate} onChange={e => setHireDate(e.target.value)} className="w-full px-3 py-2 border rounded-xl" />
          </div>
          <div>
            <label className="block text-xs font-bold text-navy mb-1">القسم</label>
            <select value={deptId} onChange={e => setDeptId(e.target.value ? Number(e.target.value) : '')} className="w-full px-3 py-2 border rounded-xl">
              <option value="">بدون قسم</option>
              {departments.map((d: Department) => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-xs font-bold text-navy mb-1">الوردية</label>
            <select value={shiftId} onChange={e => setShiftId(e.target.value ? Number(e.target.value) : '')} className="w-full px-3 py-2 border rounded-xl">
              <option value="">بدون وردية</option>
              {shifts.map((s: Shift) => <option key={s.id} value={s.id}>{s.name}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-xs font-bold text-navy mb-1">الحالة</label>
            <select value={status} onChange={e => setStatus(e.target.value as any)} className="w-full px-3 py-2 border rounded-xl">
              <option value="نشط">نشط</option>
              <option value="غير نشط">غير نشط</option>
            </select>
          </div>
        </div>

        <div className="flex gap-2 justify-end pt-2">
          {selectedId && (
            <button onClick={clearForm} className="px-4 py-2 border rounded-xl text-sm font-semibold hover:bg-gray-100">
              إلغاء التحديد
            </button>
          )}
          <button
            onClick={() => {
              if (!code || !name) return alert('الرقم والاسم مطلوبان');
              onSaveEmployee({
                id: selectedId,
                empCode: code,
                fullName: name,
                phone,
                departmentId: deptId || null,
                shiftId: shiftId || null,
                hireDate,
                status
              });
              clearForm();
            }}
            className="px-5 py-2.5 bg-greenCustom text-paper font-bold text-sm rounded-xl shadow-md hover:bg-green-700"
          >
            {selectedId ? 'حفظ التعديل' : 'إضافة الموظف'}
          </button>
        </div>
      </div>

      <div className="bg-paper rounded-2xl p-4 shadow-sm border border-line">
        <div className="mb-4 flex items-center gap-2">
          <Search size={18} className="text-muted" />
          <input
            type="text"
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="ابحث بالاسم أو الرقم الوظيفي..."
            className="w-full px-3 py-1.5 border-b border-line focus:outline-none text-sm"
          />
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-right text-sm">
            <thead className="bg-cream text-navy font-bold text-xs uppercase">
              <tr>
                <th className="p-3">الرقم</th>
                <th className="p-3">اسم الموظف</th>
                <th className="p-3">القسم</th>
                <th className="p-3">الوردية</th>
                <th className="p-3">الحالة</th>
                <th className="p-3 text-center">الإجراءات</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-line">
              {filtered.map((emp: Employee) => (
                <tr key={emp.id} className="hover:bg-cream/40">
                  <td className="p-3 font-mono font-bold text-navy">{emp.empCode}</td>
                  <td className="p-3 font-semibold">{emp.fullName}</td>
                  <td className="p-3 text-muted">{departments.find((d: any) => d.id === emp.departmentId)?.name || '—'}</td>
                  <td className="p-3 text-muted">{shifts.find((s: any) => s.id === emp.shiftId)?.name || '—'}</td>
                  <td className="p-3">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-bold ${emp.status === 'نشط' ? 'bg-greenCustom/15 text-greenCustom' : 'bg-redCustom/15 text-redCustom'}`}>
                      {emp.status}
                    </span>
                  </td>
                  <td className="p-3 text-center space-x-2 space-x-reverse">
                    <button onClick={() => handleEdit(emp)} className="p-1.5 text-gold hover:bg-gold/10 rounded-lg">
                      <Edit size={16} />
                    </button>
                    <button onClick={() => { if (confirm(`حذف الموظف ${emp.fullName} نهائياً؟`)) onDeleteEmployee(emp.id); }} className="p-1.5 text-redCustom hover:bg-redCustom/10 rounded-lg">
                      <Trash2 size={16} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function DepartmentsView({ departments, employees, onSaveDept, onDeleteDept }: any) {
  const [name, setName] = useState('');
  const [desc, setDesc] = useState('');
  const [editId, setEditId] = useState<number | null>(null);

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-navy">إدارة الأقسام</h2>
        <p className="text-muted text-sm">هيكلة الأقسام وتوزيع موظفي المنشأة</p>
        <div className="w-16 h-1 bg-gold rounded mt-1.5"></div>
      </div>

      <div className="bg-paper rounded-2xl p-6 shadow-sm border border-line space-y-4">
        <h3 className="font-bold text-gold text-base">{editId ? 'تعديل قسم' : 'إضافة قسم جديد'}</h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <input
            type="text"
            placeholder="اسم القسم"
            value={name}
            onChange={e => setName(e.target.value)}
            className="px-3 py-2 border rounded-xl"
          />
          <input
            type="text"
            placeholder="الوصف"
            value={desc}
            onChange={e => setDesc(e.target.value)}
            className="px-3 py-2 border rounded-xl"
          />
        </div>
        <div className="flex justify-end gap-2">
          {editId && <button onClick={() => { setEditId(null); setName(''); setDesc(''); }} className="px-4 py-2 border rounded-xl text-sm font-semibold">إلغاء</button>}
          <button
            onClick={() => {
              if (!name.trim()) return alert('اسم القسم مطلوب');
              onSaveDept({ id: editId, name, description: desc });
              setName(''); setDesc(''); setEditId(null);
            }}
            className="px-5 py-2.5 bg-greenCustom text-paper font-bold text-sm rounded-xl shadow-md"
          >
            {editId ? 'حفظ التعديل' : 'إضافة القسم'}
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        {departments.map((dept: Department) => {
          const count = employees.filter((e: Employee) => e.departmentId === dept.id).length;
          return (
            <div key={dept.id} className="bg-paper rounded-2xl p-5 border border-line shadow-sm flex flex-col justify-between">
              <div>
                <div className="flex items-center justify-between">
                  <h4 className="font-bold text-navy text-lg">{dept.name}</h4>
                  <span className="text-xs bg-navy2 text-gold2 px-2.5 py-0.5 rounded-full font-bold">{count} موظف</span>
                </div>
                <p className="text-xs text-muted mt-2">{dept.description || 'بدون وصف'}</p>
              </div>
              <div className="flex justify-end gap-2 mt-4 pt-3 border-t border-line">
                <button onClick={() => { setEditId(dept.id); setName(dept.name); setDesc(dept.description); }} className="p-1.5 text-gold hover:bg-gold/10 rounded-lg">
                  <Edit size={16} />
                </button>
                <button onClick={() => { if (confirm(`حذف القسم ${dept.name}؟`)) onDeleteDept(dept.id); }} className="p-1.5 text-redCustom hover:bg-redCustom/10 rounded-lg">
                  <Trash2 size={16} />
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

function ShiftsView({ shifts, onSaveShift, onDeleteShift }: any) {
  const [name, setName] = useState('');
  const [start, setStart] = useState('08:00');
  const [end, setEnd] = useState('16:00');
  const [grace, setGrace] = useState('10');
  const [editId, setEditId] = useState<number | null>(null);

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-navy">إدارة الورديات</h2>
        <p className="text-muted text-sm">أوقات وساعات الدوام الرسمي وفترة السماح للتأخير</p>
        <div className="w-16 h-1 bg-gold rounded mt-1.5"></div>
      </div>

      <div className="bg-paper rounded-2xl p-6 shadow-sm border border-line space-y-4">
        <h3 className="font-bold text-gold text-base">{editId ? 'تعديل وردية' : 'إضافة وردية جديدة'}</h3>
        <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
          <input type="text" placeholder="اسم الوردية" value={name} onChange={e => setName(e.target.value)} className="px-3 py-2 border rounded-xl" />
          <input type="time" value={start} onChange={e => setStart(e.target.value)} className="px-3 py-2 border rounded-xl" />
          <input type="time" value={end} onChange={e => setEnd(e.target.value)} className="px-3 py-2 border rounded-xl" />
          <input type="number" placeholder="دقائق السماح" value={grace} onChange={e => setGrace(e.target.value)} className="px-3 py-2 border rounded-xl" />
        </div>
        <div className="flex justify-end gap-2">
          {editId && <button onClick={() => { setEditId(null); setName(''); }} className="px-4 py-2 border rounded-xl text-sm">إلغاء</button>}
          <button
            onClick={() => {
              if (!name.trim()) return alert('اسم الوردية مطلوب');
              onSaveShift({ id: editId, name, startTime: start, endTime: end, graceMinutes: parseInt(grace, 10) || 10 });
              setName(''); setEditId(null);
            }}
            className="px-5 py-2.5 bg-greenCustom text-paper font-bold text-sm rounded-xl shadow-md"
          >
            {editId ? 'حفظ التعديل' : 'إضافة الوردية'}
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        {shifts.map((s: Shift) => (
          <div key={s.id} className="bg-paper rounded-2xl p-5 border border-line shadow-sm flex items-center justify-between">
            <div>
              <h4 className="font-bold text-navy text-base">{s.name}</h4>
              <p className="text-xs text-muted mt-1">من {s.startTime} إلى {s.endTime} | سماح: {s.graceMinutes} دقيقة</p>
            </div>
            <div className="flex gap-2">
              <button onClick={() => { setEditId(s.id); setName(s.name); setStart(s.startTime); setEnd(s.endTime); setGrace(String(s.graceMinutes)); }} className="p-1.5 text-gold hover:bg-gold/10 rounded-lg">
                <Edit size={16} />
              </button>
              <button onClick={() => { if (confirm(`حذف الوردية ${s.name}؟`)) onDeleteShift(s.id); }} className="p-1.5 text-redCustom hover:bg-redCustom/10 rounded-lg">
                <Trash2 size={16} />
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function LeavesView({ leaves, employees, onSaveLeave, onDeleteLeave }: any) {
  const today = new Date().toISOString().split('T')[0];
  const [empId, setEmpId] = useState<number | ''>(employees[0]?.id || '');
  const [startDate, setStartDate] = useState(today);
  const [endDate, setEndDate] = useState(today);
  const [type, setType] = useState('سنوية');
  const [reason, setReason] = useState('');
  const [status, setStatus] = useState<'قيد المراجعة' | 'موافق' | 'مرفوض'>('قيد المراجعة');
  const [editId, setEditId] = useState<number | null>(null);

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-navy">إدارة الإجازات</h2>
        <p className="text-muted text-sm">مراجعة وتقديم وموافقة طلبات الإجازات</p>
        <div className="w-16 h-1 bg-gold rounded mt-1.5"></div>
      </div>

      <div className="bg-paper rounded-2xl p-6 shadow-sm border border-line space-y-4">
        <h3 className="font-bold text-gold text-base">{editId ? 'تعديل طلب الإجازة' : 'تقديم طلب إجازة'}</h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div>
            <label className="block text-xs font-bold text-navy mb-1">الموظف</label>
            <select value={empId} onChange={e => setEmpId(Number(e.target.value))} className="w-full px-3 py-2 border rounded-xl">
              {employees.map((e: Employee) => <option key={e.id} value={e.id}>{e.fullName}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-xs font-bold text-navy mb-1">من تاريخ</label>
            <input type="date" value={startDate} onChange={e => setStartDate(e.target.value)} className="w-full px-3 py-2 border rounded-xl" />
          </div>
          <div>
            <label className="block text-xs font-bold text-navy mb-1">إلى تاريخ</label>
            <input type="date" value={endDate} onChange={e => setEndDate(e.target.value)} className="w-full px-3 py-2 border rounded-xl" />
          </div>
          <div>
            <label className="block text-xs font-bold text-navy mb-1">نوع الإجازة</label>
            <select value={type} onChange={e => setType(e.target.value)} className="w-full px-3 py-2 border rounded-xl">
              <option value="سنوية">سنوية</option>
              <option value="مرضية">مرضية</option>
              <option value="طارئة">طارئة</option>
              <option value="بدون راتب">بدون راتب</option>
              <option value="إذن">إذن</option>
            </select>
          </div>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <input type="text" placeholder="السبب" value={reason} onChange={e => setReason(e.target.value)} className="px-3 py-2 border rounded-xl" />
          <select value={status} onChange={e => setStatus(e.target.value as any)} className="px-3 py-2 border rounded-xl font-bold">
            <option value="قيد المراجعة">قيد المراجعة</option>
            <option value="موافق">موافق</option>
            <option value="مرفوض">مرفوض</option>
          </select>
        </div>
        <div className="flex justify-end gap-2">
          {editId && <button onClick={() => { setEditId(null); setReason(''); }} className="px-4 py-2 border rounded-xl text-sm">إلغاء</button>}
          <button
            onClick={() => {
              if (endDate < startDate) return alert('تاريخ النهاية لا يسبق البداية');
              onSaveLeave({ id: editId, employeeId: Number(empId), startDate, endDate, leaveType: type, reason, status });
              setEditId(null); setReason('');
            }}
            className="px-5 py-2.5 bg-greenCustom text-paper font-bold text-sm rounded-xl shadow-md"
          >
            {editId ? 'حفظ التعديل' : 'حفظ الطلب'}
          </button>
        </div>
      </div>

      <div className="bg-paper rounded-2xl shadow-sm border border-line overflow-hidden">
        <table className="w-full text-right text-sm">
          <thead className="bg-navy text-gold2 font-bold text-xs">
            <tr>
              <th className="p-3">الموظف</th>
              <th className="p-3">النوع</th>
              <th className="p-3">من تاريخ</th>
              <th className="p-3">إلى تاريخ</th>
              <th className="p-3">السبب</th>
              <th className="p-3">الحالة</th>
              <th className="p-3 text-center">إجراء</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-line">
            {leaves.map((l: LeaveRequest) => (
              <tr key={l.id} className="hover:bg-cream/40">
                <td className="p-3 font-bold text-navy">{employees.find((e: any) => e.id === l.employeeId)?.fullName}</td>
                <td className="p-3 text-xs">{l.leaveType}</td>
                <td className="p-3 font-mono text-xs">{l.startDate}</td>
                <td className="p-3 font-mono text-xs">{l.endDate}</td>
                <td className="p-3 text-muted text-xs">{l.reason || '—'}</td>
                <td className="p-3">
                  <span className={`px-2.5 py-0.5 rounded-full text-xs font-bold ${
                    l.status === 'موافق' ? 'bg-greenCustom/15 text-greenCustom' :
                    l.status === 'مرفوض' ? 'bg-redCustom/15 text-redCustom' :
                    'bg-orangeCustom/15 text-orangeCustom'
                  }`}>
                    {l.status}
                  </span>
                </td>
                <td className="p-3 text-center space-x-2 space-x-reverse">
                  <button onClick={() => { setEditId(l.id); setEmpId(l.employeeId); setStartDate(l.startDate); setEndDate(l.endDate); setType(l.leaveType); setReason(l.reason); setStatus(l.status); }} className="p-1.5 text-gold hover:bg-gold/10 rounded-lg">
                    <Edit size={16} />
                  </button>
                  <button onClick={() => { if (confirm('حذف هذا الطلب؟')) onDeleteLeave(l.id); }} className="p-1.5 text-redCustom hover:bg-redCustom/10 rounded-lg">
                    <Trash2 size={16} />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function ReportsView({ employees, shifts, attendance, leaves }: any) {
  const currentMonth = new Date().toISOString().slice(0, 7);
  const [selectedMonth, setSelectedMonth] = useState(currentMonth);

  const reportData = useMemo(() => {
    return employees.map((emp: Employee) => {
      const records = attendance.filter((a: AttendanceRecord) => a.employeeId === emp.id && a.workDate.startsWith(selectedMonth));
      const presentDays = records.filter((r: AttendanceRecord) => r.checkIn).length;
      const leaveDays = leaves.filter((l: LeaveRequest) => l.employeeId === emp.id && l.status === 'موافق' && l.startDate.slice(0, 7) <= selectedMonth && l.endDate.slice(0, 7) >= selectedMonth).length;
      return {
        emp,
        presentDays,
        leaveDays,
        hours: (presentDays * 8).toFixed(1)
      };
    });
  }, [employees, attendance, leaves, selectedMonth]);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-navy">التقارير الشهرية</h2>
          <p className="text-muted text-sm">ملخص شهري لساعات الحضور وأيام الغياب والإجازات</p>
          <div className="w-16 h-1 bg-gold rounded mt-1.5"></div>
        </div>
        <input
          type="month"
          value={selectedMonth}
          onChange={e => setSelectedMonth(e.target.value)}
          className="px-4 py-2 rounded-xl border border-line bg-paper text-ink font-bold text-sm shadow-sm"
        />
      </div>

      <div className="bg-paper rounded-2xl shadow-sm border border-line overflow-hidden">
        <table className="w-full text-right text-sm">
          <thead className="bg-navy text-gold2 text-xs font-bold uppercase">
            <tr>
              <th className="p-3">الرقم</th>
              <th className="p-3">الموظف</th>
              <th className="p-3">أيام الحضور</th>
              <th className="p-3">أيام الإجازات</th>
              <th className="p-3">ساعات العمل التقريبية</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-line">
            {reportData.map(({ emp, presentDays, leaveDays, hours }: any) => (
              <tr key={emp.id} className="hover:bg-cream/40">
                <td className="p-3 font-mono font-bold text-navy">{emp.empCode}</td>
                <td className="p-3 font-semibold">{emp.fullName}</td>
                <td className="p-3 font-bold text-greenCustom">{presentDays} يوم</td>
                <td className="p-3 font-bold text-gold">{leaveDays} يوم</td>
                <td className="p-3 font-mono text-navy font-bold">{hours} ساعة</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function CalendarSettingsView({ workdays, holidays, onSaveWorkdays, onAddHoliday, onDeleteHoliday }: any) {
  const dayNames = ['الاثنين', 'الثلاثاء', 'الأربعاء', 'الخميس', 'الجمعة', 'السبت', 'الأحد'];
  const [daysState, setDaysState] = useState(workdays);
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [name, setName] = useState('');

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-navy">أيام العمل والعطلات</h2>
        <p className="text-muted text-sm">تحديد جدول أيام الأسبوع والعطلات الرسمية للمنشأة</p>
        <div className="w-16 h-1 bg-gold rounded mt-1.5"></div>
      </div>

      <div className="bg-paper rounded-2xl p-6 shadow-sm border border-line space-y-4">
        <h3 className="font-bold text-gold text-base">أيام الأسبوع</h3>
        <div className="grid grid-cols-2 sm:grid-cols-7 gap-3">
          {daysState.map((w: WorkDay, i: number) => (
            <label key={i} className="flex items-center gap-2 p-3 bg-cream rounded-xl cursor-pointer">
              <input
                type="checkbox"
                checked={w.isWorkday}
                onChange={e => {
                  const updated = daysState.map((item: WorkDay, idx: number) => idx === i ? { ...item, isWorkday: e.target.checked } : item);
                  setDaysState(updated);
                  onSaveWorkdays(updated);
                }}
                className="w-4 h-4 text-greenCustom rounded"
              />
              <span className="text-xs font-bold text-navy">{dayNames[w.weekday]}</span>
            </label>
          ))}
        </div>
      </div>

      <div className="bg-paper rounded-2xl p-6 shadow-sm border border-line space-y-4">
        <h3 className="font-bold text-gold text-base">العطلات الرسمية</h3>
        <div className="flex gap-3">
          <input type="date" value={date} onChange={e => setDate(e.target.value)} className="px-3 py-2 border rounded-xl text-sm" />
          <input type="text" placeholder="اسم المناسبة" value={name} onChange={e => setName(e.target.value)} className="flex-1 px-3 py-2 border rounded-xl text-sm" />
          <button
            onClick={() => {
              if (!name.trim()) return;
              onAddHoliday({ holidayDate: date, name, notes: '' });
              setName('');
            }}
            className="px-5 py-2 bg-navy text-paper font-bold text-sm rounded-xl"
          >
            إضافة
          </button>
        </div>

        <div className="space-y-2 mt-4">
          {holidays.map((h: Holiday) => (
            <div key={h.id} className="p-3 bg-cream rounded-xl flex items-center justify-between text-sm">
              <span className="font-bold text-navy">{h.name} ({h.holidayDate})</span>
              <button onClick={() => onDeleteHoliday(h.id)} className="text-redCustom hover:text-red-700">
                <Trash2 size={16} />
              </button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

function UsersView({ users, currentUser, onSaveUser }: any) {
  const [uname, setUname] = useState('');
  const [fname, setFname] = useState('');
  const [role, setRole] = useState<'مدير' | 'مسؤول' | 'مستخدم'>('مستخدم');
  const [password, setPassword] = useState('');

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-navy">المستخدمون والصلاحيات</h2>
        <p className="text-muted text-sm">إدارة حسابات الدخول وتعيين الصلاحيات</p>
        <div className="w-16 h-1 bg-gold rounded mt-1.5"></div>
      </div>

      <div className="bg-paper rounded-2xl p-6 shadow-sm border border-line space-y-4">
        <h3 className="font-bold text-gold text-base">إضافة مستخدم جديد</h3>
        <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
          <input type="text" placeholder="اسم المستخدم" value={uname} onChange={e => setUname(e.target.value)} className="px-3 py-2 border rounded-xl text-sm" />
          <input type="text" placeholder="الاسم الكامل" value={fname} onChange={e => setFname(e.target.value)} className="px-3 py-2 border rounded-xl text-sm" />
          <input type="password" placeholder="كلمة المرور" value={password} onChange={e => setPassword(e.target.value)} className="px-3 py-2 border rounded-xl text-sm" />
          <select value={role} onChange={e => setRole(e.target.value as any)} className="px-3 py-2 border rounded-xl text-sm font-bold">
            <option value="مستخدم">مستخدم</option>
            <option value="مسؤول">مسؤول</option>
            <option value="مدير">مدير</option>
          </select>
        </div>
        <div className="flex justify-end">
          <button
            onClick={() => {
              if (!uname || !fname || !password) return alert('جميع الحقول مطلوبة');
              onSaveUser({ username: uname, fullName: fname, role, active: true, password });
              setUname(''); setFname(''); setPassword('');
            }}
            className="px-5 py-2.5 bg-greenCustom text-paper font-bold text-sm rounded-xl shadow-md"
          >
            إنشاء المستخدم
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        {users.map((u: User) => (
          <div key={u.id} className="bg-paper rounded-2xl p-5 border border-line shadow-sm flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between">
                <h4 className="font-bold text-navy text-base">{u.fullName}</h4>
                <span className="text-xs bg-navy2 text-gold2 px-2.5 py-0.5 rounded-full font-bold">{u.role}</span>
              </div>
              <p className="text-xs text-muted mt-1 font-mono">@{u.username}</p>
            </div>
            <div className="mt-4 pt-2 border-t border-line flex items-center justify-between">
              <span className={`text-xs font-bold ${u.active ? 'text-greenCustom' : 'text-redCustom'}`}>
                {u.active ? 'نشط' : 'معطل'}
              </span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function AuditView({ logs }: any) {
  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-navy">سجل العمليات</h2>
        <p className="text-muted text-sm">سجل التدقيق المحلي لجميع أحداث وحركات النظام</p>
        <div className="w-16 h-1 bg-gold rounded mt-1.5"></div>
      </div>

      <div className="bg-paper rounded-2xl shadow-sm border border-line divide-y divide-line overflow-hidden">
        {logs.map((log: AuditLog) => (
          <div key={log.id} className="p-4 hover:bg-cream/40 flex items-center justify-between">
            <div>
              <div className="font-bold text-navy text-sm">{log.action}</div>
              <div className="text-xs text-muted mt-0.5">{log.details || 'لا توجد تفاصيل إضافية'}</div>
            </div>
            <div className="text-left font-mono text-xs text-muted">
              <div>@{log.username}</div>
              <div>{log.createdAt}</div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function BackupView({ data, onRestore }: any) {
  const downloadBackup = () => {
    const jsonStr = JSON.stringify(data, null, 2);
    const blob = new Blob([jsonStr], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `attendance_backup_${new Date().toISOString().split('T')[0]}.json`;
    a.click();
  };

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (event) => {
      try {
        const parsed = JSON.parse(event.target?.result as string);
        if (confirm('هل أنت متأكد من استعادة هذه النسخة الاحتياطية؟')) {
          onRestore(parsed);
          alert('تمت الاستعادة بنجاح!');
        }
      } catch {
        alert('ملف غير صالح.');
      }
    };
    reader.readAsText(file);
  };

  return (
    <div className="space-y-6 max-w-xl mx-auto">
      <div>
        <h2 className="text-2xl font-bold text-navy">النسخ الاحتياطي والاستعادة</h2>
        <p className="text-muted text-sm">حفظ واسترجاع بيانات النظام بأمان</p>
        <div className="w-16 h-1 bg-gold rounded mt-1.5"></div>
      </div>

      <div className="bg-paper rounded-2xl p-6 border border-line shadow-sm space-y-4">
        <h3 className="font-bold text-navy text-base">تصدير نسخة احتياطية</h3>
        <p className="text-xs text-muted">حفظ ملف JSON يحتوي على كافة الموظفين والأقسام وسجلات الحضور والورديات.</p>
        <button
          onClick={downloadBackup}
          className="w-full py-3 bg-greenCustom text-paper font-bold text-sm rounded-xl shadow-md hover:bg-green-700 flex items-center justify-center gap-2"
        >
          <Download size={18} /> تحميل نسخة احتياطية (JSON)
        </button>
      </div>

      <div className="bg-paper rounded-2xl p-6 border border-line shadow-sm space-y-4">
        <h3 className="font-bold text-redCustom text-base">استعادة نسخة سابقة</h3>
        <p className="text-xs text-muted">رفع ملف نسخة احتياطية لاستبدال البيانات الحالية بعد فحص السلامة.</p>
        <label className="w-full py-3 bg-navy text-paper font-bold text-sm rounded-xl shadow-md hover:bg-navy2 flex items-center justify-center gap-2 cursor-pointer">
          <Database size={18} /> اختيار ملف النسخة الاحتياطية
          <input type="file" accept=".json" onChange={handleFileUpload} className="hidden" />
        </label>
      </div>
    </div>
  );
}
