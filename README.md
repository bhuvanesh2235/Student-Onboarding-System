# 🎓 Student Onboarding System

## 📌 Overview
The **Student Onboarding System** is a full-stack application that automates student data generation, validation, storage, and management.

It consists of:
- 🐍 Python Data Pipeline (Service A)
- ☕ Spring Boot Backend API (Service B)
- ⚛️ React Frontend
- 🐘 PostgreSQL Database
- 🐳 Docker Containerization (Full System Orchestration)

---

## 🖼️ Project Output

![Student Onboarding System Output](./Student%20Onboarding%20System%20Output.png)

---

## 🚀 End-to-End Workflow

CSV Generation → Data Validation → REST API → Database → UI Display

---

## 🧩 Tech Stack

| Layer          | Technology                              |
|----------------|------------------------------------------|
| Data Pipeline  | Python (pandas, requests, watchdog)      |
| Backend        | Spring Boot, JPA, Hibernate              |
| Frontend       | React                                    |
| Database       | PostgreSQL                               |
| DevOps         | Docker, Docker Compose, Nginx            |

---

## 🐍 Service A – Python Pipeline

### Features:
- Generates synthetic student data (with invalid cases)
- Validates data (email, name, age)
- Splits into valid & invalid CSV
- Sends valid data to backend in batches
- Retry logic for API failures

### Key Files:
- `main.py` → Orchestrates pipeline
- `generator.py` → Generates CSV data
- `validator.py` → Validates records
- `sender.py` → Sends data to backend
- `watcher.py` → Monitors directory

---

## ☕ Service B – Spring Boot Backend

### Features:
- REST APIs for CRUD operations
- Bulk insert endpoint
- Pagination support
- Validation using DTOs

### APIs:
- `POST /students/bulk` → Bulk insert
- `GET /students` → Paginated fetch
- `PUT /students/{id}` → Update
- `DELETE /students/{id}` → Delete

---

## ⚛️ Frontend – React

### Features:
- View students (table)
- Add / Edit / Delete students
- Pagination support
- Form validation
- API integration

### Key Files:
- `App.js` → Main logic
- `StudentForm.js` → Add/Edit form
- `StudentTable.js` → Data display
- `api.js` → API calls

---

## 🐘 Database – PostgreSQL

- Table: `students`
- Fields:
  - `id` (auto-generated)
  - `name`
  - `email` (unique)
  - `age`

---

## 🐳 Docker & Containerization

### Overview
The entire system is containerized using Docker and orchestrated via Docker Compose.

### Services:
- **postgres** → Database container
- **backend** → Spring Boot service
- **python** → Data pipeline (batch execution)
- **frontend** → React app served via Nginx

### Key Features:
- No hardcoded `localhost` (uses Docker service names)
- Environment variable-based configuration
- Nginx reverse proxy for frontend → backend communication
- Health checks and proper startup order
- Fully isolated and scalable architecture

### Architecture Flow:
Postgres → Backend → Python Pipeline  
Backend → Frontend (via Nginx proxy `/api`)

---

## 🌐 Access (Docker)
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/students
  
## ✅ Key Highlights
- Automated data pipeline with validation
- Batch processing with retry logic
- Scalable REST API with pagination
- Full CRUD operations
- Interactive frontend UI
- Docker-based microservices architecture
- End-to-end system integration

## 🎯 Conclusion

This project simulates a real-world onboarding system combining:

- Data Engineering
- Backend Development
- Frontend UI
- Database Integration
- DevOps (Docker & Containerization)

It demonstrates strong full-stack development and system design skills.
