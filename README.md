# 🎓 Student Management System

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.11-brightgreen)]()
[![Java](https://img.shields.io/badge/Java-21-orange)]()
[![JWT](https://img.shields.io/badge/JWT-Security-blue)]()
[![MongoDB](https://img.shields.io/badge/MongoDB-NoSQL-green)]()
[![Docker](https://img.shields.io/badge/Docker-Ready-blue)]()
[![License](https://img.shields.io/badge/License-MIT-green)]()

> **Full Stack Student Management System** with Spring Boot backend, HTML/CSS/JS frontend, and a comprehensive audit log powered by MongoDB.

---

## ✨ Features

### Backend
- ✅ JWT Authentication & Authorization (with refresh tokens)
- ✅ Role-based access control (`ROLE_ADMIN`, `ROLE_USER`, etc.)
- ✅ Full CRUD operations for students
- ✅ Advanced search (name, surname, email)
- ✅ Filter by age range
- ✅ Pagination support
- ✅ Custom exception handling & validation
- ✅ Email scheduler (hourly student count)
- ✅ Rate limiting (5 requests/second on `/apis/register`)
- ✅ AOP logging
- ✅ Swagger/OpenAPI documentation
- ✅ H2 in‑memory database (development) & MySQL (production) support
- ✅ **Audit Log** with MongoDB – tracks every action:
  - Student actions (created, updated, deleted, viewed, searched)
  - Authentication events (login, register, token refresh)
  - Rate limit violations

### Frontend
- ✅ Pure HTML/CSS/JavaScript (no frameworks)
- ✅ Bootstrap 5 + custom glassmorphism styling
- ✅ Dark/light mode toggle
- ✅ Fully responsive (mobile‑friendly)
- ✅ JWT token management (localStorage)
- ✅ Protected routes
- ✅ Student dashboard with real‑time search, filter, pagination
- ✅ CRUD modals with validation
- ✅ Toast notifications
- ✅ **Audit Log panel** – administrators can view and filter all logged events

---

## 🛠 Tech Stack

| Layer       | Technologies |
|-------------|--------------|
| **Backend** | Java 21, Spring Boot 3.5.11, Spring Security, Spring Data JPA, Spring Data MongoDB, JWT, ModelMapper, Bucket4j, Lombok |
| **Frontend**| HTML5, CSS3, JavaScript (ES6), Bootstrap 5 |
| **Database**| H2 (development), MySQL (production), **MongoDB** (audit logs) |
| **DevOps**  | Docker, Docker Compose |
| **Docs**    | Swagger/OpenAPI |

---

## 🚀 Quick Start

### Prerequisites
- Java 21
- Maven
- Docker & Docker Compose (recommended)
- Node.js (optional, for frontend development)

### Run with Docker Compose (easiest)
```bash
# Clone the repository
git clone https://github.com/huseyinkarimli/Student-Management-System.git
cd Student-Management-System

# Build and start all services (backend, frontend, mongodb)
docker-compose up --build -d
```

Then access:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:student_db`, user: `root`, pass: `1234`)

### Run without Docker
**Backend:**
```bash
cd backend
mvn spring-boot:run
```

**Frontend:**  
Open `frontend/index.html` with a live server (e.g., VS Code Live Server) on port `5500` or `3000`.

---

## 📚 API Documentation (selected endpoints)

### Authentication (`/apis`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/apis/register` | Register a new user |
| POST | `/apis/login` | Login, returns JWT & refresh token |
| POST | `/apis/refresh-token` | Refresh access token |

### Students (`/api/students`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/students` | Get all students (for current user) |
| GET | `/api/students/{id}` | Get student by ID |
| POST | `/api/students` | Create a new student |
| PUT | `/api/students/{id}` | Update a student |
| DELETE | `/api/students/{id}` | Delete a student |
| GET | `/api/students/search?query=` | Search by name/email/surname |
| GET | `/api/students/filter/age?minAge=&maxAge=` | Filter by age range |
| GET | `/api/students/page?begin=&length=` | Paginated list |
| GET | `/api/students/count` | Total student count (for current user) |

### Audit Logs (`/api/audit`) – require `ROLE_ADMIN`
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/audit/student/{studentId}` | Logs for a specific student |
| GET | `/api/audit/user/{userId}` | Logs for a specific user |
| GET | `/api/audit/range?start=...&end=...` | Logs within a date range |
| GET | `/api/audit/action/{action}` | Logs for a specific action type |

---

## 🗄️ Database Structure

### Relational (H2/MySQL) – student data
- `users`, `roles`, `user_roles`
- `students` (id, name, surname, email, age, created_at, user_id)

### NoSQL (MongoDB) – audit logs
- Collection: `audit_logs`
- Document fields: `id`, `timestamp`, `userId`, `username`, `action`, `details`, `ipAddress`, `affectedStudentId`

---

## 👤 Default Users (from `data.sql`)

| Username | Password | Roles |
|----------|----------|-------|
| u1 | 123 | `ROLE_ADMIN`, `ROLE_GET_STUDENTS`, `ROLE_ADD_STUDENT`, ... (all permissions) |
| u2 | 456 | `ROLE_GET_STUDENTS`, `ROLE_GET` |

Use `u1` to access the **Audit Log** panel.

---

## 🐳 Docker Compose Services

The `docker-compose.yml` defines:
- `backend` – built from `./backend/Dockerfile` (multi‑stage)
- `frontend` – built from `./frontend/Dockerfile` (served by nginx, proxies API requests)
- `mongodb` – official `mongo:latest` image, data persisted in a named volume

---

## 📁 Project Structure

```
Student-Management-System/
├── backend/                 # Spring Boot application
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── ...
├── frontend/                # HTML/CSS/JS frontend
│   ├── index.html
│   ├── css/
│   ├── js/
│   ├── pages/
│   ├── nginx.conf           # custom nginx config for API proxying
│   └── Dockerfile
├── docker-compose.yml
├── .gitignore
└── README.md
```

---

## 👨‍💻 Author
**Huseyn Karimli**   
LinkedIn: [Huseyn Karimli](https://linkedin.com/in/huseyin-karimli)

---

⭐ **If you like this project, give it a star!**