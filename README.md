# Multi-Tenant SaaS Subscription Platform

A production-grade multi-tenant SaaS platform built with Java Spring Boot, PostgreSQL, and React.js. Designed from a full BRD and TDD before writing any code.

## Live Demo
> Deployment in progress — AWS EC2 + RDS

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.5.1, Spring Security 6, Spring Data JPA |
| Auth | JWT (access token + refresh token), RBAC |
| Database | PostgreSQL 15, Flyway migrations |
| Frontend | React.js (in progress) |
| Cloud | AWS EC2, RDS PostgreSQL |
| Build | Maven, Java 17, Docker |

## Architecture
React SPA → Spring Boot REST API → PostgreSQL (AWS RDS)

- Row-level multi-tenancy — every table has `tenant_id`
- JWT stateless auth with role-based access (OWNER / ADMIN / MEMBER)
- Subscription plan gating enforced server-side (Free / Pro / Enterprise)
- Flyway versioned migrations — zero manual DDL

## Features

- ✅ Tenant registration and onboarding
- ✅ JWT authentication — register, login, token refresh
- ✅ Role-based access control (OWNER, ADMIN, MEMBER)
- ✅ Project management — full CRUD with pagination
- ✅ Subscription plan enforcement — Free plan limited to 3 projects
- ⬜ Task management (in progress)
- ⬜ Team member invitation
- ⬜ React frontend
- ⬜ AWS deployment

## API Endpoints

### Auth
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | /api/auth/register | None | Register + create tenant |
| POST | /api/auth/login | None | Login, returns JWT |

### Projects
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | /api/projects | JWT | List all projects (paginated) |
| POST | /api/projects | JWT | Create project |
| GET | /api/projects/{id} | JWT | Get project by ID |
| PUT | /api/projects/{id} | JWT | Update project |
| DELETE | /api/projects/{id} | JWT | Delete project |

## Getting Started

### Prerequisites
- Java 17+
- Docker Desktop
- Maven

### Run locally

```bash
# 1. Start PostgreSQL
docker compose up -d

# 2. Run the app
./mvnw spring-boot:run
```

### Environment Variables

| Variable | Description |
|---|---|
| DB_URL | JDBC URL to PostgreSQL |
| DB_USERNAME | Database username |
| DB_PASSWORD | Database password |
| JWT_SECRET | Min 256-bit base64 string |
| CORS_ALLOWED_ORIGINS | Frontend origin URL |

## Project Structure

src/main/java/com/saas/platform/
├── controller/       # REST controllers
├── service/          # Business logic + plan enforcement
├── repository/       # Data access with tenant isolation
├── entity/           # JPA entities + enums
├── dto/              # Request/response DTOs
├── security/         # JWT filter, UserDetails
├── config/           # Security, CORS config
└── exception/        # Global exception handler

## Documentation
- BRD v1.0 — Business requirements, RBAC matrix, subscription plan matrix
- TDD v1.0 — Database schema, API contracts, security design, AWS architecture

## Author
**Chinmay J** — [github.com/ChinmayCJ7](https://github.com/ChinmayCJ7) | [linkedin.com/in/chinmay-j](https://linkedin.com/in/chinmay-j)

