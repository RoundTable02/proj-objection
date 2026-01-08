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

# Docker build (local)
docker build -t proj-objection:test .

# Docker run (local test)
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e MYSQL_URL='jdbc:mysql://host:3306/objection_db' \
  -e MYSQL_USERNAME='user' \
  -e MYSQL_PASSWORD='pass' \
  proj-objection:test
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
- **Config** (`config/`): `LoginUserArgumentResolver` resolves `@LoginUser` parameters, `WebConfig` registers resolvers, `SwaggerConfig` configures OpenAPI documentation

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

### Swagger (API Documentation)

API 명세서는 springdoc-openapi를 사용합니다.

**접근 경로:**
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

**Controller 어노테이션 컨벤션:**

```java
@Tag(name = "도메인명", description = "API 그룹 설명")
@RestController
public class XxxController {

    @Operation(summary = "API 요약", description = "API 상세 설명")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공 설명"),
            @ApiResponse(responseCode = "400", description = "실패 설명",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class)))
    })
    @PostMapping("/endpoint")
    public BaseResponse<ResultDto> method(@RequestBody RequestDto request) { }
}
```

**DTO 어노테이션 컨벤션:**

```java
@Schema(description = "DTO 설명")
@Getter
public class XxxDto {

    @Schema(description = "필드 설명", example = "예시값")
    private String field;
}
```

**@LoginUser 파라미터는 Swagger에서 숨김 처리:**

```java
public BaseResponse<String> method(@Parameter(hidden = true) @LoginUser User user) { }
```

**설정 변경:** `application.yml`의 `springdoc` 섹션에서 API 제목, 설명, 버전 수정 가능

## Package Structure

The base package is `kuit.hackathon.proj_objection` (note: underscore, not hyphen).

## Deployment

### CD Pipeline

GitHub Actions를 통한 자동 배포 (main 브랜치 push 시):

```
GitHub (main push) → GitHub Actions → Docker Hub → EC2 (docker pull & run) → RDS
```

### Key Files

- `Dockerfile`: Multi-stage build (Gradle 8.14 → Eclipse Temurin 17 JRE)
- `.github/workflows/deploy.yml`: CD workflow
- `.dockerignore`: Docker build exclusions
- `application-prod.yml`: Production config (uses env vars)

### GitHub Secrets Required

| Secret | Description |
|--------|-------------|
| `DOCKERHUB_USERNAME` | Docker Hub username |
| `DOCKERHUB_TOKEN` | Docker Hub access token |
| `EC2_HOST` | EC2 public IP |
| `EC2_USERNAME` | SSH username (ubuntu/ec2-user) |
| `EC2_SSH_KEY` | EC2 SSH private key |
| `MYSQL_URL` | RDS connection URL |
| `MYSQL_USERNAME` | RDS username |
| `MYSQL_PASSWORD` | RDS password |
