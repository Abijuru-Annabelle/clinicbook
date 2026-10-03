# ClinicBook — Clinic Appointment Booking System

Web Technology project · AUCA · 2026-2027 · Instructor: Jeremie U. Tuyisenge

Patients in Rwanda often book clinic visits in person or by phone, leading to long queues, double bookings and missed appointments.
**ClinicBook** lets patients book, reschedule and cancel appointments online, lets doctors manage their availability and daily schedule,
and lets clinic admins manage departments and doctors. Patients and doctors receive email/SMS notifications.

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | React 19 (Vite), React Router, Tailwind CSS, Axios |
| Backend | Spring Boot 3.5 (Java 17), layered architecture |
| Relational DB | PostgreSQL 17 — users, doctors, departments, availability, appointments |
| Non-relational DB | MongoDB — medical documents (GridFS), notification logs |
| Messaging | RabbitMQ — email / SMS / reminder events, retry + dead-letter queue |
| Security | JWT (OAuth2 resource server), Google OAuth2 login, BCrypt, RBAC with `@PreAuthorize` |
| Performance | Caffeine cache, gzip compression, HikariCP, JOIN FETCH queries, frontend code splitting |
| Testing | JUnit 5, Mockito, Spring Security Test (MockMvc) |

## Architecture

```
React (5173) ──REST/JSON + JWT──▶ Spring Boot API (8080)
                                   ├─ web        (controllers, RBAC)
                                   ├─ service    (business rules)
                                   ├─ repository (JPA → PostgreSQL)
                                   ├─ mongo      (MongoDB + GridFS)
                                   └─ messaging  ──▶ RabbitMQ ──▶ email / SMS consumers ──▶ MongoDB logs
```

RabbitMQ topology: topic exchange `clinicbook.events` →
`notifications.email` (all events) and `notifications.sms` (`appointment.*`).
Failed messages are retried 3 times, then routed to `clinicbook.dlx` → `notifications.dead`.

## Roles (RBAC)

| Role | Can do |
|---|---|
| PATIENT | Find doctors, book / reschedule / cancel own appointments, upload medical files |
| DOCTOR | View daily schedule, mark COMPLETED / NO_SHOW, manage weekly availability, view files |
| ADMIN | Manage departments and doctors |

## Run locally

Requirements: Java 17, Node.js 20+, PostgreSQL, MongoDB, RabbitMQ (with Erlang 28) running locally.

1. Create the database: `CREATE DATABASE clinicbook;`
2. Create `backend/secrets.properties` (not committed):
   ```properties
   spring.datasource.password=YOUR_POSTGRES_PASSWORD
   spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
   spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
   ```
3. Start the backend (tables and demo data are created automatically):
   ```bash
   cd backend
   ./mvnw spring-boot:run      # Windows: .\mvnw.cmd spring-boot:run
   ```
4. Start the frontend:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
5. Open http://localhost:5173

### Demo accounts

| Role | Email | Password |
|---|---|---|
| Patient | patient@clinicbook.rw | Patient@123 |
| Doctor | doctor@clinicbook.rw | Doctor@123 |
| Admin | admin@clinicbook.rw | Admin@123 |

### Run the tests

```bash
cd backend
./mvnw test
```

## Main API endpoints

| Method | Endpoint | Role |
|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | public |
| GET | `/oauth2/authorization/google` | public (Google login) |
| GET | `/api/doctors`, `/api/doctors/{id}/slots?date=` | any logged-in |
| POST | `/api/appointments` | PATIENT |
| GET | `/api/appointments/me` | PATIENT |
| PATCH | `/api/appointments/{id}/cancel`, `/reschedule` | PATIENT |
| PATCH | `/api/appointments/{id}/status` | DOCTOR |
| GET/POST/DELETE | `/api/doctor/availability` | DOCTOR |
| POST/DELETE | `/api/departments`, `/api/doctors` | ADMIN |
| GET/POST | `/api/appointments/{id}/documents` | PATIENT, DOCTOR |
| GET | `/api/notifications/me` | any logged-in |
