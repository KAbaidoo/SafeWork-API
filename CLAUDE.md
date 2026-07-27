# CLAUDE.md (Monorepo)

This file provides guidance when working with code in this repository.

## Project Structure
- `api/`: Spring Boot backend (Java 17, Maven)
- `web/`: React Web Dashboard (Vite, TypeScript, npm)
- `docs/`: Consolidated product specifications and PRDs
- `mobile/`: Placeholder for future mobile app

## Common Commands

### Backend (api/ directory)
- `cd api && ./mvnw clean test` - Run all backend tests
- `cd api && ./mvnw spring-boot:run` - Run development API server (port 8081)
- `cd api && ./mvnw test -Dtest=ClassName` - Run specific backend test class
- `cd api && ./mvnw test -Dtest=ClassName#methodName` - Run specific backend test method

### Frontend (web/ directory)
- `cd web && npm install` - Install frontend dependencies
- `cd web && npm run dev` - Start React dev server (port 5173)
- `cd web && npm run build` - Build frontend assets
- `cd web && npm run typecheck` - Run TypeScript compiler checks
- `cd web && npm run lint` - Run ESLint checks

## Development Conventions
- **Multi-Tenant Isolation**: Enforce organization-scoped data access in the service layer using `@AuthenticationPrincipal User`.
- **Entity Design**: Use selective Lombok annotations (`@Getter`, `@Setter`, etc.) instead of `@Data` on JPA entities to avoid `LazyInitializationException` and hashing issues.
- **DTOs**: Use Java records for immutable request and response models.
- **Testing**: Maintain the 5-layer test strategy (Repository, Service, Controller, Integration, Mapper) for new features.
