# Student Management System (EduCore SMS)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.11-brightgreen)]()
[![Java](https://img.shields.io/badge/Java-21-orange)]()
[![JWT](https://img.shields.io/badge/JWT-Security-blue)]()
[![MongoDB](https://img.shields.io/badge/MongoDB-Audit%20logs-green)]()
[![Docker](https://img.shields.io/badge/Docker-Compose-blue)]()
[![License](https://img.shields.io/badge/License-MIT-green)]()

Full-stack student management: Spring Boot REST API, vanilla HTML/CSS/JavaScript frontend, MySQL for transactional data, and MongoDB for audit logs.

---

## Features

### Backend

- **Authentication:** JWT access tokens, refresh tokens, stateless sessions.
- **Role-based access:** `ROLE_ADMIN`, `ROLE_TEACHER`, `ROLE_STUDENT`, `ROLE_USER`, plus fine-grained student permissions (`ROLE_GET_STUDENTS`, `ROLE_ADD_STUDENT`, etc.). Method-level security with `@PreAuthorize`.
- **Students:** CRUD, search (name / surname / email), age filter, pagination, per-user ownership where applicable.
- **Courses:** CRUD (create/update/delete restricted to admin or teacher); list/detail for authenticated users.
- **Enrollments:** List by student or course; create, update grade, and delete (admin only).
- **Assignments:** CRUD per course (admin/teacher); read for authenticated users.
- **Submissions:** Submit (student/admin), list by assignment or student, grade (admin/teacher).
- **Attendance:** Single and batch recording; list by course or student; stats for student and course; update/delete (admin/teacher); course-scoped checks for teachers.
- **Teacher API:** Dashboard summary, teacher’s courses, assignments, pending submissions (`ROLE_TEACHER`).
- **Admin API:** List users, get/update user roles, list all roles, list users with `ROLE_TEACHER`.
- **Audit logging (MongoDB):** Persists actions such as student/course/enrollment/assignment/submission/attendance events, auth events (login, register, token refresh, logout), and rate-limit violations. Admin-only REST queries by student, user, date range, or action type.
- **Cross-cutting:** Global exception handling, validation, AOP logging, SpringDoc OpenAPI (Swagger UI).
- **Rate limiting (Bucket4j):** `POST /apis/register` — 5 requests per minute per IP; `POST /apis/login` and `POST /apis/refresh-token` — 10 per minute per IP; violations are audit-logged.
- **Email scheduler:** Hourly job sends total student count via Spring Mail (configure SMTP in `application.properties`).

### Frontend

- **Stack:** HTML5, CSS3, JavaScript (ES6), Bootstrap 5–style layout with custom styling (glassmorphism-style UI).
- **Auth:** Login and registration pages; JWT and refresh token in **sessionStorage**; shared `apiFetch` with 401 redirect and 403 handling.
- **Layout:** Sidebar navigation, role-based sections (teacher panel, admin users + audit).
- **Student dashboard:** Metrics and student CRUD with search, filters, pagination, modals, toasts.
- **Courses:** Course management; **My courses** for enrolled views tied to backend data.
- **Assignments:** Course assignments and submission flow where implemented in UI.
- **Teacher dashboard:** Teacher-focused overview (courses, assignments, pending work).
- **Attendance:** Teacher/admin attendance UI; **My attendance** for students (stats/history).
- **Admin:** User management (roles assignment against backend admin API).
- **Audit log panel:** Admins filter and inspect MongoDB-backed audit entries.
- **UX:** Dark/light theme (persisted in `localStorage`), responsive layout, Font Awesome icons.

---

## Tech stack

| Layer | Technologies |
|--------|----------------|
| **Backend** | Java 21, Spring Boot 3.5.11, Spring Security, Spring Data JPA, Spring Data MongoDB, Spring Mail, Spring AOP, JWT, ModelMapper 3.1.1, Bucket4j 7.6.0, Lombok, SpringDoc OpenAPI 2.8.0 |
| **Frontend** | HTML, CSS, JavaScript (ES6), CDN fonts (Clash Display, Cabinet Grotesk), Font Awesome 6 |
| **Data** | **MySQL** (primary JPA store), **MongoDB** (audit collection `audit_logs`) |
| **Ops** | Docker, Docker Compose, Nginx (frontend image proxies `/api` and `/apis` to the backend) |

---

## Quick start (Docker Compose)

### Prerequisites

- Docker and Docker Compose
- **MySQL 8** listening on the **host** at port **3306**, database `student_management` creatable (or auto-created), user/password matching `application.properties` / compose env (default in repo: `root` / `1234`)

The backend container uses `host.docker.internal` to reach MySQL on the machine running Docker. Adjust `SPRING_DATASOURCE_URL` in `docker-compose.yml` if your setup differs.

### Run

```bash
git clone https://github.com/huseyinkarimli/student-management-system.git
cd student-management-system
docker compose up --build -d
```

### URLs

| Service | URL |
|---------|-----|
| Frontend (Nginx) | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html (or `/swagger-ui.html`) |
| MongoDB | localhost:27017 (database `student_audit` by default) |

Compose sets `SPRING_PROFILES_ACTIVE=development`, wires MongoDB, optional JWT overrides (`JWT_SECRET`, `JWT_REFRESH_SECRET`), and `SERVER_FORWARD_HEADERS_STRATEGY=framework` for correct client IP/scheme behind Nginx.

### Local development without Docker

1. Start MongoDB locally and MySQL with a `student_management` schema.
2. Backend: `cd backend` then `mvn spring-boot:run` (ensure `application.properties` points to your MySQL and MongoDB).
3. Frontend: serve the `frontend` folder over HTTP (e.g. Live Server). CORS allows `localhost:5500` and `localhost:3000` in the development security profile.

---

## API summary

Base paths: **`/apis`** (auth), **`/api`** (application). All `/api/**` routes require a valid JWT except as noted for Swagger.

### Auth — `/apis`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/apis/register` | Register (rate limited) |
| POST | `/apis/login` | Login; returns tokens (rate limited) |
| POST | `/apis/refresh-token` | Refresh access token (rate limited) |
| GET | `/apis/add`, `/apis/get`, `/apis/update`, `/apis/delete` | Legacy demo endpoints; need `ROLE_ADD` / `ROLE_GET` / `ROLE_UPDATE` / `ROLE_DELETE` |

### Students — `/api/students`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/students` | List (needs `ROLE_GET_STUDENTS`) |
| GET | `/api/students/search` | Search |
| GET | `/api/students/{id}` | By id (`ROLE_GET_STUDENT`) |
| POST | `/api/students` | Create (`ROLE_ADD_STUDENT`) |
| PUT | `/api/students/{id}` | Update (`ROLE_UPDATE_STUDENT`) |
| DELETE | `/api/students/{id}` | Delete (`ROLE_DELETE_STUDENT`) |
| GET | `/api/students/page` | Pagination |
| GET | `/api/students/filter/age` | Age range |
| GET | `/api/students/search/name`, `/search/surname` | Search variants |
| GET | `/api/students/count` | Count |

### Courses — `/api/courses`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/courses` | List |
| GET | `/api/courses/{id}` | Detail |
| POST | `/api/courses` | Create (admin or teacher) |
| PUT | `/api/courses/{id}` | Update (admin or teacher) |
| DELETE | `/api/courses/{id}` | Delete (admin or teacher) |

### Enrollments — `/api/enrollments`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/enrollments/student/{studentId}` | By student |
| GET | `/api/enrollments/course/{courseId}` | By course |
| POST | `/api/enrollments` | Enroll (admin) |
| PUT | `/api/enrollments/{id}/grade` | Update grade (admin) |
| DELETE | `/api/enrollments/{id}` | Remove (admin) |

### Assignments — `/api/assignments`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/assignments/course/{courseId}` | By course |
| GET | `/api/assignments/{id}` | By id |
| POST | `/api/assignments` | Create (admin or teacher) |
| PUT | `/api/assignments/{id}` | Update (admin or teacher) |
| DELETE | `/api/assignments/{id}` | Delete (admin or teacher) |

### Submissions — `/api/submissions`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/submissions/assignment/{assignmentId}` | By assignment (admin or teacher) |
| GET | `/api/submissions/student/{studentId}` | By student |
| POST | `/api/submissions` | Submit (admin or student) |
| PUT | `/api/submissions/{id}/grade` | Grade (admin or teacher) |

### Attendance — `/api/attendance`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/attendance` | Record (admin or teacher) |
| POST | `/api/attendance/batch` | Batch (admin or teacher) |
| GET | `/api/attendance/course/{courseId}` | List for course |
| GET | `/api/attendance/student/{studentId}` | List for student |
| GET | `/api/attendance/student/{studentId}/stats` | Student stats |
| GET | `/api/attendance/course/{courseId}/stats` | Course stats |
| PUT | `/api/attendance/{id}` | Update (admin or teacher) |
| DELETE | `/api/attendance/{id}` | Delete (admin or teacher) |

### Teachers — `/api/teachers` (requires `ROLE_TEACHER`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/teachers/dashboard` | Dashboard DTO |
| GET | `/api/teachers/courses` | Teacher courses |
| GET | `/api/teachers/assignments` | Teacher assignments |
| GET | `/api/teachers/submissions/pending` | Pending submissions |

### Admin — `/api/admin` (requires `ROLE_ADMIN`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/admin/users` | All users |
| GET | `/api/admin/users/{id}/roles` | Roles for user |
| PUT | `/api/admin/users/{id}/roles` | Replace roles |
| GET | `/api/admin/roles` | All roles |
| GET | `/api/admin/teachers` | Users with `ROLE_TEACHER` |

### Audit — `/api/audit` (requires `ROLE_ADMIN`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/audit/student/{studentId}` | By affected student |
| GET | `/api/audit/user/{userId}` | By user id |
| GET | `/api/audit/range?start=&end=` | Time range |
| GET | `/api/audit/action/{action}` | By `AuditAction` enum name |

---

## Database overview

### MySQL (JPA)

Typical tables: `users`, `roles`, join table for user–roles, `students`, `courses`, `enrollments`, `assignments`, `submissions`, `attendance`. Hibernate `ddl-auto` is `update` in the bundled config (adjust for production).

### MongoDB

- **Database:** `student_audit` (overridable via `SPRING_DATA_MONGODB_URI`)
- **Collection:** `audit_logs`
- **Fields:** `id`, `timestamp`, `userId`, `username`, `action`, `details`, `ipAddress`, `affectedStudentId`

---

## Default users and seeding

SQL seeding is disabled (`spring.sql.init.mode=never`). Users and roles are created by `DatabaseSeeder` on startup.

| Username | Password | Notes |
|----------|----------|--------|
| `admin` | `Admin@1234` | Default admin; configurable via `app.admin.username` / `app.admin.password` |
| `teacher1` | `Teacher@1234` | Created only if `app.seeder.seed-test-data=true` |
| `student1` | `Student@1234` | Created only if `app.seeder.seed-test-data=true` |

The admin user receives the full role set used by the app (including `ROLE_ADMIN`, `ROLE_TEACHER`, `ROLE_STUDENT`, `ROLE_USER`, and student CRUD/search roles). Change passwords after deployment.

---

## Project structure

```
student-management-system/
├── backend/
│   ├── src/main/java/az/developia/studentmanagement/
│   │   ├── audit/           # MongoDB audit documents & services
│   │   ├── config/          # Security, OpenAPI, seeder, etc.
│   │   ├── controller/      # REST controllers
│   │   ├── dto/, entity/, repository/, service/
│   │   ├── filter/          # JWT, rate limiting
│   │   ├── scheduler/       # Email scheduler
│   │   └── ...
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── index.html, login.html, register.html
│   ├── css/, js/            # app.js, layout.js, per-page scripts
│   ├── pages/
│   │   ├── dashboard.html, students.html, courses.html, my-courses.html
│   │   ├── teacher-dashboard.html, student-attendance.html, audit.html
│   │   ├── admin/users.html
│   │   └── courses/assignments.html, courses/attendance.html
│   ├── nginx.conf
│   └── Dockerfile
├── docker-compose.yml
└── README.md
```

---

## Docker Compose services

| Service | Role |
|---------|------|
| `backend` | Spring Boot JAR on port 8080 |
| `frontend` | Nginx serving static files on port 3000; proxies `/api` and `/apis` to `backend:8080` |
| `mongodb` | MongoDB 7 with persisted volume `mongodb-data` |

---

## Production and external access (short notes)

- Override **JWT** secrets with environment variables (`JWT_SECRET`, `JWT_REFRESH_SECRET`); do not rely on repository defaults in production.
- Nginx sets `X-Forwarded-*` headers; Compose sets `SERVER_FORWARD_HEADERS_STRATEGY=framework` so Spring sees the correct scheme and client chain.
- For HTTPS, secrets, CORS tightening, and port-forwarding caveats, treat the above as the minimum; use a reverse proxy or tunnel with TLS for anything beyond local or lab use.
- Configure or disable the **mail scheduler** and SMTP credentials for your environment; the bundled Gmail placeholders are for development only.

---

## Author

**Huseyin Karimli**  
LinkedIn:(https://linkedin.com/in/huseyin-karimli)
