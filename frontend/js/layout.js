// Shared sidebar component
// Uses root-relative /pages/ paths so navigation works from any page depth
function renderSidebar(_depth, activePage) {
  const P = '/pages';
  return `
<aside class="sidebar" id="sidebar">
  <div class="sidebar-logo">
    <div class="logo-mark">🎓</div>
    <span class="logo-text">EduCore SMS</span>
  </div>
  <div class="sidebar-body">
    <div class="nav-group">
      <div class="nav-group-label">Əsas</div>
      <a href="${P}/dashboard.html" class="nav-link ${activePage==='dashboard'?'active':''}">
        <i class="ni fa-solid fa-chart-pie"></i> Dashboard
      </a>
      <a href="${P}/students.html" class="nav-link ${activePage==='students'?'active':''}">
        <i class="ni fa-solid fa-users"></i> Tələbələr
      </a>
      <a href="${P}/courses.html" class="nav-link ${activePage==='courses'?'active':''}">
        <i class="ni fa-solid fa-book-open"></i> Kurslar
      </a>
      <a href="${P}/my-courses.html" class="nav-link ${activePage==='my-courses'?'active':''}">
        <i class="ni fa-solid fa-graduation-cap"></i> Mənim kurslarım
      </a>
    </div>
    <div class="nav-group">
      <div class="nav-group-label">Akademik</div>
      <a href="${P}/courses/assignments.html" class="nav-link ${activePage==='assignments'?'active':''}">
        <i class="ni fa-solid fa-file-lines"></i> Tapşırıqlar
      </a>
      <a href="${P}/courses/attendance.html" class="nav-link ${activePage==='attendance'?'active':(activePage==='teacher-attendance'?'active':'')}">
        <i class="ni fa-solid fa-calendar-check"></i> Davamiyyət
      </a>
      <a href="${P}/student-attendance.html" class="nav-link ${activePage==='student-attendance'?'active':''}">
        <i class="ni fa-solid fa-chart-bar"></i> Mənim davamiyyətim
      </a>
    </div>
    <div class="nav-group teacher-only" style="display:none">
      <div class="nav-group-label">Müəllim</div>
      <a href="${P}/teacher-dashboard.html" class="nav-link ${activePage==='teacher-dashboard'?'active':''}">
        <i class="ni fa-solid fa-chalkboard-user"></i> Müəllim paneli
      </a>
    </div>
    <div class="nav-group admin-only" style="display:none">
      <div class="nav-group-label">Admin</div>
      <a href="${P}/admin/users.html" class="nav-link ${activePage==='admin-users'?'active':''}">
        <i class="ni fa-solid fa-user-gear"></i> İstifadəçilər
      </a>
      <a href="${P}/audit.html" class="nav-link ${activePage==='audit'?'active':''}">
        <i class="ni fa-solid fa-shield-halved"></i> Audit Log
      </a>
    </div>
  </div>
  <div class="sidebar-foot">
    <div class="user-tile" onclick="logout()">
      <div class="user-av" id="sidebar-av">U</div>
      <div class="user-info">
        <div class="user-name" id="sidebar-username">İstifadəçi</div>
        <div class="user-role">EduCore SMS</div>
      </div>
      <button class="btn-logout" title="Çıxış">
        <i class="fa-solid fa-right-from-bracket"></i>
      </button>
    </div>
  </div>
</aside>`;
}

function renderTopbar(title) {
  return `
<header class="topbar">
  <span class="topbar-title">${title}</span>
  <div class="topbar-actions">
    <button class="icon-btn" id="theme-btn" onclick="toggleTheme()" title="Tema">
      <i class="fa-solid fa-sun"></i>
    </button>
  </div>
</header>`;
}

function baseHead(title, depth) {
  const d = depth || '';
  return `
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${title} — EduCore SMS</title>
  <link rel="preconnect" href="https://fonts.googleapis.com"/>
  <link href="https://fonts.googleapis.com/css2?family=Clash+Display:wght@400;500;600;700&family=Cabinet+Grotesk:wght@300;400;500;700&display=swap" rel="stylesheet"/>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css"/>
  <link rel="stylesheet" href="${d}css/app.css"/>
  <script>
    (function(){ const t=localStorage.getItem('theme'); if(t==='light') document.documentElement.setAttribute('data-theme','light'); })();
  </script>`;
}
