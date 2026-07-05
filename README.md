# Employee Management System (EMS)

A production-inspired **Full Stack Employee Management System** built with **Spring Boot**, **Next.js**, **PostgreSQL**, and **Docker**.

This project was developed as part of a **Full Stack Software Developer practise** and demonstrates modern full-stack engineering practices including JWT authentication, role-based authorization, REST APIs, Flyway migrations, Docker, automated testing, GitHub Actions, and a responsive frontend built with **Vanilla CSS**.

---

# Features

## Authentication

* JWT Authentication
* Stateless Spring Security
* Email & Password Login
* User Registration
* BCrypt Password Encryption
* Role-Based Access Control (RBAC)

Roles

* **ADMIN**

  * Manage Employees
  * Manage User Roles
  * Full CRUD Access

* **USER**

  * View Employees
  * Read-only Access

---

# Employee Management

* Create Employee
* View Employees
* Update Employee
* Delete Employee
* Search
* Pagination
* Sorting
* Form Validation
* CSV Export
* JSON Export

---

# Backend Features

* Spring Boot 3
* Java 21
* Spring Security
* Spring Data JPA
* JWT Authentication
* PostgreSQL
* Flyway Database Migrations
* Global Exception Handling
* Bean Validation
* OpenAPI / Swagger
* Caffeine Cache
* Bucket4j Rate Limiting
* SLF4J Logging

---

# Frontend Features

* Next.js App Router
* TypeScript
* TanStack Query
* Zustand
* React Hook Form
* Zod Validation
* Axios Interceptors
* React Toastify
* Responsive Dashboard
* Protected Routes
* Role-Based UI
* Vanilla CSS (No Tailwind)

---

# Technology Stack

## Frontend

* Next.js
* React
* TypeScript
* TanStack Query
* Zustand
* React Hook Form
* Zod
* Axios
* React Toastify
* Vanilla CSS

## Backend

* Java 21
* Spring Boot
* Spring Security
* Spring Data JPA
* Flyway
* PostgreSQL
* JWT
* Maven

## DevOps

* Docker
* Docker Compose
* GitHub Actions

---

# Project Structure

```text
.
├── backend/
├── frontend/
├── docker-compose.yml
├── README.md
└── .github/
    └── workflows/
```

---

# Architecture

```text
Browser
    │
    ▼
Next.js Frontend
    │
    ▼
Spring Boot REST API
    │
    ▼
Spring Security (JWT)
    │
    ▼
Spring Data JPA
    │
    ▼
PostgreSQL
```

---

# Default Accounts

## Administrator

Email

```
admin@pesira.local
```

Password

```
admin123
```

---

## Standard User

Email

```
user@pesira.local
```

Password

```
user123
```

---

# Running Locally

## Requirements

* Java 21
* Node.js 22+
* Docker Desktop
* Docker Compose

---

## Backend

```bash
cd backend
./mvnw spring-boot:run
```

Backend

```
http://localhost:8080
```

---

## Frontend

```bash
cd frontend

npm install

npm run dev
```

Frontend

```
http://localhost:3000
```

---

# Running with Docker

Build and start all services

```bash
docker compose up --build
```

Run in detached mode

```bash
docker compose up -d --build
```

Stop containers

```bash
docker compose down
```

Stop containers and remove database volume

```bash
docker compose down -v
```

Rebuild everything

```bash
docker compose build --no-cache
docker compose up -d
```

---

# Docker Services

The Docker Compose configuration starts:

* PostgreSQL
* Spring Boot Backend
* Next.js Frontend

Ports

| Service    | Port |
| ---------- | ---- |
| Frontend   | 3000 |
| Backend    | 8080 |
| PostgreSQL | 5432 |

---

# Viewing Logs

All services

```bash
docker compose logs -f
```

Backend

```bash
docker compose logs -f backend
```

Frontend

```bash
docker compose logs -f frontend
```

Database

```bash
docker compose logs -f postgres
```

---

# Running Tests

## Backend

Run all tests

```bash
cd backend

./mvnw test
```

Run a specific test

```bash
./mvnw -Dtest=EmployeeServiceTest test
```

---

## Frontend

Run tests

```bash
cd frontend

npm test
```

Run lint

```bash
npm run lint
```

Build production

```bash
npm run build
```

---

# API Documentation

Swagger UI

```
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON

```
http://localhost:8080/v3/api-docs
```

---

# API Features

* JWT Authentication
* CRUD Operations
* Pagination
* Sorting
* Search
* Validation
* CSV Export
* JSON Export
* Global Exception Handling

---

# Environment Variables

Backend

```env
DB_HOST=postgres
DB_PORT=5432
DB_NAME=pesira_ems
DB_USER=pesira
DB_PASSWORD=pesira

JWT_SECRET=your-secret-key
```

Frontend

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

---

# Security

* JWT Authentication
* BCrypt Password Hashing
* Spring Security
* Role-Based Authorization
* Rate Limiting
* Request Validation
* Protected API Endpoints

---

# Database

Database schema is managed using **Flyway**.

All migrations are automatically executed during application startup.

No manual SQL setup is required.

---

# Continuous Integration

GitHub Actions automatically performs:

* Backend Build
* Backend Tests
* Frontend Build
* Frontend Tests
* Lint Checks

Every push and pull request is validated automatically.

---

# Design Decisions

This project follows a layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

Key design decisions include:

* DTOs instead of exposing JPA entities.
* Constructor-based dependency injection.
* Stateless JWT authentication.
* Centralized exception handling.
* Flyway for schema versioning.
* TanStack Query for server state.
* Zustand for client-side state.
* React Hook Form with Zod for form validation.
* Dockerized development environment.

---

# Future Improvements

Potential enhancements include:

* Email verification.
* Password reset.
* Audit history.
* Soft deletes.
* Redis caching.
* User profile management.
* Refresh tokens.
* Dark mode.
* Advanced filtering.
* Metrics and monitoring with Prometheus/Grafana.

---

# License

This project was developed solely for educational and assessment purposes.
