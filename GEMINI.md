# GEMINI.md

This document provides a comprehensive guide for working with the SafeWork API codebase.

## Monorepo Restructuring Notice
The backend API is now located in the `api/` directory. When running Maven commands or configuring environment variables, please navigate into the `api/` folder:
```bash
cd api
```

## Project Overview

SafeWork is a mobile-first operations platform designed to help small and medium-sized businesses manage safety, compliance, and asset maintenance. The backend is a Spring Boot 3.5.4 application written in Java 17. It follows a domain-driven design, is multi-tenant, and uses JWT for authentication. The API is the single source of truth for both mobile and web clients, with a focus on reliable offline functionality through version-based synchronization.

**Key Technologies:**
*   **Backend:** Spring Boot 3.5.4 (Java 17)
*   **Database:** PostgreSQL with Flyway for migrations
*   **Authentication:** JWT
*   **Build Tool:** Maven
*   **Testing:** JUnit 5, Mockito, H2 (for tests)

## Building and Running

### Environment Setup

The application requires the following environment variables to be set:

```bash
export DB_PASSWORD=your_production_password
export DB_DEV_PASSWORD=your_dev_password
export JWT_SECRET=your_jwt_secret_minimum_256_bits

# For development with provided sample credentials:
export DB_DEV_PASSWORD=Warhammer2000
export JWT_SECRET=33bb10979d8682ce60f944465eaeca3fa8ffcaf6e7c246a43a5f0f769c58bd37
```

### Maven Commands

All commands should be executed from the `api/` subdirectory:

*   **Run the application (dev profile):**
    ```bash
    cd api
    ./mvnw spring-boot:run
    ```
    The application will be available at `http://localhost:8081/api`.

*   **Build and run all tests:**
    ```bash
    cd api
    ./mvnw clean test
    ```

*   **Build a JAR file:**
    ```bash
    cd api
    ./mvnw clean package
    ```

*   **Run specific tests:**
    ```bash
    cd api
    # Run a specific test class
    ./mvnw test -Dtest=AssetRepositoryTest

    # Run a specific test method
    ./mvnw test -Dtest=AssetRepositoryTest#shouldCreateAssetWithRelationships
    ```

## Development Conventions

### Code Style and Architecture

*   **Domain-Driven Design:** The code is organized by business domain (e.g., `asset`, `user`, `organization`). Each domain has its own `controller`, `service`, `repository`, `model`, `dto`, and `mapper` packages.
*   **Java Records for DTOs:** All Data Transfer Objects (DTOs) use Java records for immutability and conciseness.
*   **Lombok:** Used for dependency injection (`@RequiredArgsConstructor`) and boilerplate reduction. Note the specific conventions for entities to avoid issues with JPA (e.g., no `@Data` on entities).
*   **Validation:** Jakarta Bean Validation (`@Valid`, `@NotBlank`, etc.) is used for request validation.

### Testing

The project has a comprehensive 5-layer testing strategy for each domain:
1.  **Repository Tests (`@DataJpaTest`)**
2.  **Service Tests (Mockito)**
3.  **Controller Tests (`@WebMvcTest`)**
4.  **Integration Tests (`@SpringBootTest`)**
5.  **Mapper Tests (Plain JUnit)**

When adding new features, please add corresponding tests following the existing structure.
