# Project: Multi-Tenant SaaS Subscription Platform

## Developer
- Name: Chinmay J
- College: P.E.S. College of Engineering, Mandya, Karnataka (graduating 2026)
- Goal: Campus placement portfolio project

## Tech Stack
- Backend: Spring Boot 3.5.1, Spring Security 6, Spring Data JPA, JWT (jjwt 0.12.3)
- Database: PostgreSQL 15 (Docker local, RDS production), Flyway migrations
- Frontend: React.js (not started yet)
- Cloud: AWS EC2 + RDS (not deployed yet)
- Build: Maven, Java 17

## Project Structure
- Package root: com.saas.platform
- Packages: entity, entity/enums, repository, service, controller, dto/request, dto/response, security, config, exception

## Current Progress
✅ Spring Boot 3.5.1 scaffolded
✅ PostgreSQL running in Docker (docker-compose.yml in project root)
✅ 7 Flyway migrations applied (V1-V7) — 6 tables, 10 indexes
✅ 6 entities: Tenant, User, RefreshToken, Subscription, Project, Task
✅ 6 enums: Plan, PlanStatus, Role, BillingCycle, SubscriptionStatus, ProjectStatus, TaskStatus, Priority
✅ 6 repositories with tenant-scoped queries
✅ Security layer: JwtService, JwtAuthFilter, TenantAwareUserDetails, SecurityConfig
✅ Exception handling: GlobalExceptionHandler, ErrorResponse, custom exceptions
✅ Auth: AuthService, AuthController — POST /api/auth/register and POST /api/auth/login working in Postman

## Next Step
Build the Projects module:
- ProjectService.java (with plan limit enforcement)
- ProjectController.java (GET, POST, PUT, DELETE /api/projects)
- CreateProjectRequest.java, ProjectResponse.java DTOs

## Key Design Rules (enforce these silently)
- Every tenant-scoped query MUST filter by tenantId — use findByIdAndTenantId, findAllByTenantId
- DTOs only in controller responses — never expose @Entity directly
- @Transactional on all service write methods
- Passwords hashed with BCrypt, never plain
- All secrets via environment variables (jwt.secret, DB credentials)
- Plan limits enforced in service layer — Free=3 projects, Pro=unlimited, Enterprise=unlimited
- Feature gating returns HTTP 403 with error code PLAN_LIMIT_EXCEEDED

## Plan Limits
- FREE: max 3 projects, max 5 members
- PRO: unlimited projects, max 25 members
- ENTERPRISE: unlimited everything

## Database (docker-compose)
- DB: saasdb, User: saasuser, Password: localpassword, Port: 5432

## application.yml keys
- jwt.secret: local dev secret (min 32 chars)
- jwt.access-token-expiry-ms: 900000 (15 min)
- app.cors.allowed-origins: http://localhost:5173,http://localhost:3000

## BRD and TDD
- BRD v1.0 and TDD v1.0 documents generated and saved locally
- 26 functional requirements defined in BRD
- Full API contracts, DB schema, security design in TDD

## Git
- Repo: github.com/ChinmayCJ7/saas-platform (set up but not pushed yet)
- Branching: feature branches, meaningful commit messages