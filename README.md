# SafeWork Monorepo

SafeWork is a mobile-first operations and compliance platform designed to help small and medium-sized businesses manage assets, conduct inspections, and improve safety and efficiency.

This repository houses the entire workspace including backend API services, front-end web dashboard, product documentation, and future mobile app development.

## Structure
- `/api`: Spring Boot 3.5.4 backend written in Java 17.
- `/web`: React 18 + Vite + TypeScript web dashboard component.
- `/docs`: Consolidated product vision, Master PRD, design documentation, and API structure.
- `/mobile`: Future mobile client workspace.

## Quick Start

### Running the Backend
Ensure you have the required environment variables set:
```bash
export DB_DEV_PASSWORD=Warhammer2000
export JWT_SECRET=33bb10979d8682ce60f944465eaeca3fa8ffcaf6e7c246a43a5f0f769c58bd37
```
Run the Spring Boot application (dev profile is active by default, running on port 8081):
```bash
cd api
./mvnw spring-boot:run
```
The API is available at `http://localhost:8081/api`.

### Running the Web Dashboard
```bash
cd web
npm install
npm run dev
```
The dashboard runs at `http://localhost:5173`.
