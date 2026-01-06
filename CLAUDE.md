# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Development Commands

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "kuit.hackathon.proj_objection.ProjObjectionApplicationTests"

# Clean build artifacts
./gradlew clean
```

## Architecture Overview

This is a Spring Boot 4.0.1 application using Java 17 with a layered architecture:

**Controller → Service → Repository → Entity**

### Key Components

- **Controllers** (`controller/`): REST endpoints - `LoginController` handles authentication, `TestController` for testing/session verification
- **Services** (`service/`): Business logic layer - `LoginService` manages login, password validation, and auto-creates new users on first login
- **Repositories** (`repository/`): JPA data access - `UserRepository` for User entity operations
- **Entities** (`entity/`): Domain models - `User` and `BaseEntity` (provides JPA audit fields)
- **DTOs** (`dto/`): `BaseResponse<T>` for success, `BaseErrorResponse` for errors
- **Exception Handling** (`exception/`): `MainExceptionHandler` using `@RestControllerAdvice` catches `BaseException` subclasses
- **Annotations** (`annotation/`): Custom annotations - `@LoginUser` for injecting authenticated user into controller methods
- **Config** (`config/`): `LoginUserArgumentResolver` resolves `@LoginUser` parameters, `WebConfig` registers resolvers

### Authentication Flow

1. User sends nickname + password to `/login`
2. If user doesn't exist, auto-creates account with BCrypt-hashed password
3. If user exists, validates password
4. Creates HTTP session with userId stored in session attributes
5. Session-based auth (not token-based), 30-minute timeout

### Using @LoginUser

To get the authenticated user in a controller method, use `@LoginUser User user`:

```java
@GetMapping("/me")
public BaseResponse<String> getMyInfo(@LoginUser User user) {
    return new BaseResponse<>(user.getNickname());
}
```

The `LoginUserArgumentResolver` automatically extracts userId from the session and fetches the User entity. Throws `UserNotFoundException` if not logged in or user doesn't exist.

### Database

- MySQL 8.0.32 on localhost:3306
- Database: `objection_db`
- Hibernate DDL: `create` mode (development only - switch to `update` or `none` for production)

## Package Structure

The base package is `kuit.hackathon.proj_objection` (note: underscore, not hyphen).
