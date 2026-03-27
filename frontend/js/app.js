/* EduCore SMS — Shared JS Utilities */
const BASE = '';
const API = {
  AUTH: '/apis',
  STUDENTS: '/api/students',
  AUDIT: '/api/audit',
  ADMIN: '/api/admin',
  COURSES: '/api/courses',
  ENROLLMENTS: '/api/enrollments',
  ASSIGNMENTS: '/api/assignments',
  SUBMISSIONS: '/api/submissions',
  TEACHERS: '/api/teachers',
  ATTENDANCE: '/api/attendance',
};

// Token stored in sessionStorage (matches backend expectation)
const Auth = {
  getToken: () => sessionStorage.getItem('jwt_token'),
  setToken: (t) => sessionStorage.setItem('jwt_token', t),
  setRefresh: (t) => sessionStorage.setItem('jwt_refresh', t),
  getRefresh: () => sessionStorage.getItem('jwt_refresh'),
  setUser: (u) => sessionStorage.setItem('sms_user', u),
  getUser: () => sessionStorage.getItem('sms_user') || 'İstifadəçi',
  setRoles: (r) => sessionStorage.setItem('sms_roles', JSON.stringify(r)),
  getRoles: () => { try { return JSON.parse(sessionStorage.getItem('sms_roles') || '[]'); } catch(e) { return []; } },
  hasRole: (r) => Auth.getRoles().includes(r),
  isAdmin: () => Auth.hasRole('ROLE_ADMIN'),
  isTeacher: () => Auth.hasRole('ROLE_TEACHER'),
  clear: () => { sessionStorage.clear(); },
  check: () => { if (!Auth.getToken()) { redirectToLogin(); return false; } return true; }
};

function redirectToLogin() {
  window.location.href = '/login.html';
}

async function parseError(res) {
  const ct = res.headers.get('content-type') || '';
  const txt = await res.text();
  if (!txt) return 'Xəta baş verdi';
  if (ct.includes('application/json')) {
    try {
      const j = JSON.parse(txt);
      if (Array.isArray(j) && j.length) return j.map(e => (e.field ? e.field + ': ' : '') + e.message).join('. ');
      return j.message || j.error || txt;
    } catch(_) {}
  }
  return txt;
}

async function apiFetch(path, opts = {}) {
  const url = path.startsWith('http') ? path : BASE + path;
  const token = Auth.getToken();
  const headers = { ...(opts.headers || {}) };
  if (token) headers['Authorization'] = 'Bearer ' + token;
  if (opts.body && typeof opts.body === 'string') headers['Content-Type'] = 'application/json';
  const res = await fetch(url, { ...opts, headers });
  const isProtected = [API.STUDENTS, API.AUDIT, API.ADMIN, API.COURSES, API.ENROLLMENTS, API.ASSIGNMENTS, API.SUBMISSIONS, API.TEACHERS, API.ATTENDANCE].some(p => url.includes(p));
  if (isProtected) {
    if (res.status === 401) { Auth.clear(); redirectToLogin(); throw new Error('Sessiyanız başa çatıb'); }
    if (res.status === 403) { throw new Error(await parseError(res) || 'Bu əməliyyat üçün icazəniz yoxdur'); }
  }
  return res;
}

// ── TOAST ──
function showToast(msg, type = 'success') {
  let host = document.getElementById('toast-host');
  if (!host) { host = document.createElement('div'); host.id = 'toast-host'; host.className = 'toast-host'; document.body.appendChild(host); }
  const t = document.createElement('div');
  const icons = { success: 'fa-circle-check', error: 'fa-circle-exclamation', warning: 'fa-triangle-exclamation', info: 'fa-circle-info' };
  t.className = 'toast ' + (type === 'error' ? 'error' : type === 'warning' ? 'warning' : type === 'info' ? 'info' : 'success');
  t.innerHTML = `<i class="toast-icon fa-solid ${icons[type] || icons.success}"></i><span class="toast-msg">${msg}</span>`;
  host.appendChild(t);
  setTimeout(() => { t.style.animation = 'toastOut 0.3s ease forwards'; setTimeout(() => t.remove(), 320); }, 3600);
}

// ── MODAL HELPERS ──
function openModal(id) { document.getElementById(id)?.classList.add('open'); }
function closeModal(id) { document.getElementById(id)?.classList.remove('open'); }
function bindModalClose(id) {
  const ov = document.getElementById(id);
  if (!ov) return;
  ov.addEventListener('click', e => { if (e.target === ov) closeModal(id); });
  document.addEventListener('keydown', e => { if (e.key === 'Escape') closeModal(id); });
}

// ── THEME ──
function initTheme() {
  const t = localStorage.getItem('theme') || 'dark';
  if (t === 'light') document.documentElement.setAttribute('data-theme', 'light');
  const btn = document.getElementById('theme-btn');
  if (btn) { btn.innerHTML = t === 'light' ? '<i class="fa-solid fa-moon"></i>' : '<i class="fa-solid fa-sun"></i>'; }
}
function toggleTheme() {
  const curr = document.documentElement.getAttribute('data-theme') || 'dark';
  const next = curr === 'dark' ? 'light' : 'dark';
  document.documentElement.setAttribute('data-theme', next);
  localStorage.setItem('theme', next);
  const btn = document.getElementById('theme-btn');
  if (btn) btn.innerHTML = next === 'light' ? '<i class="fa-solid fa-moon"></i>' : '<i class="fa-solid fa-sun"></i>';
}

// ── SIDEBAR USER ──
function initSidebarUser() {
  const u = Auth.getUser();
  const el = document.getElementById('sidebar-username');
  const av = document.getElementById('sidebar-av');
  if (el) el.textContent = u;
  if (av) av.textContent = u[0]?.toUpperCase() || 'U';
  // Show admin-only nav items
  if (Auth.isAdmin()) {
    document.querySelectorAll('.admin-only').forEach(el => el.style.display = '');
  }
  if (Auth.isTeacher()) {
    document.querySelectorAll('.teacher-only').forEach(el => el.style.display = '');
  }
}

function logout() { Auth.clear(); redirectToLogin(); }

// ── TABLE SORT ──
function makeSortable(arr, col, dir) {
  return [...arr].sort((a, b) => {
    const va = String(a[col] ?? '').toLowerCase();
    const vb = String(b[col] ?? '').toLowerCase();
    if (!isNaN(va) && !isNaN(vb)) return dir === 'asc' ? Number(va) - Number(vb) : Number(vb) - Number(va);
    return dir === 'asc' ? va.localeCompare(vb) : vb.localeCompare(va);
  });
}

// ── PAGINATION ──
function renderPagination(containerId, total, perPage, current, onPage) {
  const el = document.getElementById(containerId);
  if (!el) return;
  const pages = Math.ceil(total / perPage);
  if (pages <= 1) { el.innerHTML = ''; return; }
  const from = (current - 1) * perPage + 1;
  const to = Math.min(current * perPage, total);
  let btns = '';
  for (let i = 1; i <= pages; i++) {
    if (i === 1 || i === pages || Math.abs(i - current) <= 1) {
      btns += `<button class="pg-btn${i === current ? ' active' : ''}" onclick="(${onPage})(${i})">${i}</button>`;
    } else if (Math.abs(i - current) === 2) {
      btns += `<span style="padding:0 4px;color:var(--text3)">…</span>`;
    }
  }
  el.innerHTML = `
    <span class="pg-info">${from}–${to} / ${total}</span>
    <div class="pg-btns">
      <button class="pg-btn" onclick="(${onPage})(${current - 1})" ${current === 1 ? 'disabled' : ''}><i class="fa-solid fa-chevron-left" style="font-size:10px"></i></button>
      ${btns}
      <button class="pg-btn" onclick="(${onPage})(${current + 1})" ${current === pages ? 'disabled' : ''}><i class="fa-solid fa-chevron-right" style="font-size:10px"></i></button>
    </div>`;
}

// ── EXCEL EXPORT (SheetJS required) ──
function exportExcel(data, filename, sheetName) {
  if (!window.XLSX) { showToast('Excel kitabxanası yüklənməyib', 'error'); return; }
  if (!data.length) { showToast('İxrac üçün məlumat yoxdur', 'warning'); return; }
  const ws = XLSX.utils.json_to_sheet(data);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, sheetName || 'Data');
  XLSX.writeFile(wb, filename || 'export.xlsx');
  showToast('Excel faylı yükləndi');
}

// ── API CALLS ──
const api = {
  // Auth
  login: async (u, p) => {
    const r = await apiFetch(API.AUTH + '/login', { method: 'POST', body: JSON.stringify({ username: u, password: p }) });
    if (!r.ok) throw new Error(await parseError(r));
    return r.json();
  },
  register: async (u, p) => {
    const r = await apiFetch(API.AUTH + '/register', { method: 'POST', body: JSON.stringify({ username: u, password: p }) });
    if (!r.ok) throw new Error(await parseError(r));
    const ct = r.headers.get('content-type') || '';
    return ct.includes('json') ? r.json() : { message: await r.text() };
  },

  // Students
  students: {
    all: async () => { const r = await apiFetch(API.STUDENTS); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    search: async (q) => { const r = await apiFetch(API.STUDENTS + '/search?query=' + encodeURIComponent(q)); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    count: async () => { const r = await apiFetch(API.STUDENTS + '/count'); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    byAge: async (min, max) => { const r = await apiFetch(API.STUDENTS + '/filter/age?minAge=' + min + '&maxAge=' + max); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    create: async (b) => { const r = await apiFetch(API.STUDENTS, { method: 'POST', body: JSON.stringify(b) }); if (!r.ok) throw new Error(await parseError(r)); },
    update: async (id, b) => { const r = await apiFetch(API.STUDENTS + '/' + id, { method: 'PUT', body: JSON.stringify({ id, ...b }) }); if (!r.ok) throw new Error(await parseError(r)); },
    delete: async (id) => { const r = await apiFetch(API.STUDENTS + '/' + id, { method: 'DELETE' }); if (!r.ok) throw new Error(await parseError(r)); },
  },

  // Courses
  courses: {
    all: async () => { const r = await apiFetch(API.COURSES); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    get: async (id) => { const r = await apiFetch(API.COURSES + '/' + id); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    create: async (b) => { const r = await apiFetch(API.COURSES, { method: 'POST', body: JSON.stringify(b) }); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    update: async (id, b) => { const r = await apiFetch(API.COURSES + '/' + id, { method: 'PUT', body: JSON.stringify(b) }); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    delete: async (id) => { const r = await apiFetch(API.COURSES + '/' + id, { method: 'DELETE' }); if (!r.ok) throw new Error(await parseError(r)); },
  },

  // Enrollments
  enrollments: {
    byStudent: async (id) => { const r = await apiFetch(API.ENROLLMENTS + '/student/' + id); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    byCourse: async (id) => { const r = await apiFetch(API.ENROLLMENTS + '/course/' + id); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    enroll: async (sId, cId) => { const r = await apiFetch(API.ENROLLMENTS, { method: 'POST', body: JSON.stringify({ studentId: sId, courseId: cId }) }); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    drop: async (id) => { const r = await apiFetch(API.ENROLLMENTS + '/' + id, { method: 'DELETE' }); if (!r.ok) throw new Error(await parseError(r)); },
    grade: async (id, grade) => { const r = await apiFetch(API.ENROLLMENTS + '/' + id + '/grade', { method: 'PUT', body: JSON.stringify({ grade }) }); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
  },

  // Assignments
  assignments: {
    byCourse: async (cId) => { const r = await apiFetch(API.ASSIGNMENTS + '/course/' + cId); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    get: async (id) => { const r = await apiFetch(API.ASSIGNMENTS + '/' + id); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    create: async (b) => { const r = await apiFetch(API.ASSIGNMENTS, { method: 'POST', body: JSON.stringify(b) }); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    update: async (id, b) => { const r = await apiFetch(API.ASSIGNMENTS + '/' + id, { method: 'PUT', body: JSON.stringify(b) }); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    delete: async (id) => { const r = await apiFetch(API.ASSIGNMENTS + '/' + id, { method: 'DELETE' }); if (!r.ok) throw new Error(await parseError(r)); },
  },

  // Submissions
  submissions: {
    byAssignment: async (id) => { const r = await apiFetch(API.SUBMISSIONS + '/assignment/' + id); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    byStudent: async (id) => { const r = await apiFetch(API.SUBMISSIONS + '/student/' + id); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    submit: async (b) => { const r = await apiFetch(API.SUBMISSIONS, { method: 'POST', body: JSON.stringify(b) }); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    grade: async (id, score, feedback) => { const r = await apiFetch(API.SUBMISSIONS + '/' + id + '/grade', { method: 'PUT', body: JSON.stringify({ score, feedback }) }); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
  },

  // Attendance
  attendance: {
    record: async (b) => { const r = await apiFetch(API.ATTENDANCE, { method: 'POST', body: JSON.stringify(b) }); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    batch: async (b) => { const r = await apiFetch(API.ATTENDANCE + '/batch', { method: 'POST', body: JSON.stringify(b) }); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    byCourseDate: async (cId, date) => { const r = await apiFetch(API.ATTENDANCE + '/course/' + cId + '?date=' + date); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    byStudent: async (sId, cId) => { const r = await apiFetch(API.ATTENDANCE + '/student/' + sId + '?courseId=' + cId); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    statsStudent: async (sId, cId) => { const r = await apiFetch(API.ATTENDANCE + '/student/' + sId + '/stats?courseId=' + cId); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    statsCourse: async (cId) => { const r = await apiFetch(API.ATTENDANCE + '/course/' + cId + '/stats'); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    update: async (id, b) => { const r = await apiFetch(API.ATTENDANCE + '/' + id, { method: 'PUT', body: JSON.stringify(b) }); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    delete: async (id) => { const r = await apiFetch(API.ATTENDANCE + '/' + id, { method: 'DELETE' }); if (!r.ok) throw new Error(await parseError(r)); },
  },

  // Audit
  audit: {
    byStudent: async (id) => { const r = await apiFetch(API.AUDIT + '/student/' + id); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    byUser: async (id) => { const r = await apiFetch(API.AUDIT + '/user/' + id); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    byRange: async (s, e) => { const r = await apiFetch(API.AUDIT + '/range?start=' + s + '&end=' + e); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    byAction: async (a) => { const r = await apiFetch(API.AUDIT + '/action/' + encodeURIComponent(a)); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
  },

  // Admin
  admin: {
    users: async () => { const r = await apiFetch(API.ADMIN + '/users'); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    userRoles: async (id) => { const r = await apiFetch(API.ADMIN + '/users/' + id + '/roles'); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    updateRoles: async (id, roles) => { const r = await apiFetch(API.ADMIN + '/users/' + id + '/roles', { method: 'PUT', body: JSON.stringify({ roles }) }); if (!r.ok) throw new Error(await parseError(r)); },
    roles: async () => { const r = await apiFetch(API.ADMIN + '/roles'); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    teachers: async () => { const r = await apiFetch(API.ADMIN + '/teachers'); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
  },

  // Teachers
  teachers: {
    dashboard: async () => { const r = await apiFetch(API.TEACHERS + '/dashboard'); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    courses: async () => { const r = await apiFetch(API.TEACHERS + '/courses'); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    assignments: async () => { const r = await apiFetch(API.TEACHERS + '/assignments'); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
    pendingSubmissions: async () => { const r = await apiFetch(API.TEACHERS + '/submissions/pending'); if (!r.ok) throw new Error(await parseError(r)); return r.json(); },
  },
};
