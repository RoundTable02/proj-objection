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

- **Controllers** (`controller/`): REST endpoints - `LoginController` handles authentication, `TestController` for testing/session verification, `ChatRoomController` for chatroom management, `ChatMessageController` for messaging (REST + WebSocket)
- **Services** (`service/`): Business logic layer - `LoginService` manages login, `ChatRoomService` handles chatroom creation/joining, `ChatMessageService` handles message sending/retrieval
- **Repositories** (`repository/`): JPA data access - `UserRepository`, `ChatRoomRepository`, `ChatRoomMemberRepository`, `ChatMessageRepository`
- **Entities** (`entity/`): Domain models - `User`, `ChatRoom`, `ChatRoomMember`, `ChatMessage`, `BaseEntity` (provides JPA audit fields)
- **DTOs** (`dto/`): `BaseResponse<T>` for success, `BaseErrorResponse` for errors, `ChatMessageDto`, `ChatMessageListDto`, etc.
- **Exception Handling** (`exception/`): `MainExceptionHandler` using `@RestControllerAdvice` catches `BaseException` subclasses
- **Annotations** (`annotation/`): Custom annotations - `@LoginUser` for injecting authenticated user into controller methods
- **Config** (`config/`): `LoginUserArgumentResolver` resolves `@LoginUser` parameters, `WebConfig` registers resolvers, `SwaggerConfig` configures OpenAPI documentation, `WebSocketConfig` configures STOMP WebSocket

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

### Chat Room System

채팅방 시스템은 두 종류의 참여자를 지원합니다:
- **PARTICIPANT**: 대화 상대방 - 메시지 전송 가능
- **OBSERVER**: 관전자 - 메시지 조회만 가능 (전송 불가)

**주요 엔티티:**
- `ChatRoom`: 채팅방 정보 (title, participantCode, observerCode, creator)
- `ChatRoomMember`: 채팅방 멤버 정보 (chatRoom, user, role)
- `ChatMessage`: 채팅 메시지 (chatRoom, sender, content)

**API 엔드포인트:**
- `POST /chat/room/create`: 채팅방 생성 (생성자는 자동으로 PARTICIPANT로 입장)
- `POST /chat/room/join`: 초대 코드로 입장 (participantCode → PARTICIPANT, observerCode → OBSERVER)
- `POST /chat/room/{chatRoomId}/message`: 메시지 전송 (REST)
- `GET /chat/room/{chatRoomId}/messages`: 메시지 목록 조회

**초대 코드:**
- 채팅방 생성 시 두 개의 초대 코드 자동 생성 (형식: `0000-0000`)
- `participantCode`: 대화 상대방 초대용
- `observerCode`: 관전자 초대용

### WebSocket (STOMP) Chat

실시간 채팅은 STOMP 프로토콜 기반 WebSocket을 사용합니다.

**연결 설정:**
- WebSocket 엔드포인트: `/ws` (SockJS 지원), `/ws` (순수 WebSocket)
- 메시지 브로커: `/topic` prefix
- 애플리케이션 목적지: `/app` prefix

**메시지 전송:**
```javascript
// 클라이언트에서 메시지 전송
stompClient.send("/app/chatroom/{chatRoomId}", {}, JSON.stringify({content: "Hello"}));
```

**메시지 수신:**
```javascript
// 채팅방 구독
stompClient.subscribe("/topic/chatroom/{chatRoomId}", function(message) {
    // 메시지 처리
});
```

**세션 연동:**
- HTTP 세션의 userId가 WebSocket 핸드셰이크 시 WebSocket 세션으로 복사됨
- `HttpSessionHandshakeInterceptor`가 이 작업을 담당
- WebSocket 메시지 처리 시 `SimpMessageHeaderAccessor`에서 userId 추출 가능

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
