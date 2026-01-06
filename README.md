# Objection Project

Spring Boot 기반의 백엔드 서버 프로젝트입니다.

## 기술 스택

- Java 17
- Spring Boot 4.0.1
- Spring Data JPA
- MySQL 8.0.32
- Gradle 9.2.1

## 시작하기

### 사전 요구사항

- JDK 17
- MySQL 8.0+

### 데이터베이스 설정

```sql
CREATE DATABASE objection_db;
CREATE USER 'objection_admin'@'localhost' IDENTIFIED BY 'objection1234';
GRANT ALL PRIVILEGES ON objection_db.* TO 'objection_admin'@'localhost';
FLUSH PRIVILEGES;
```

### 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun

# 테스트
./gradlew test

# 단일 테스트 실행
./gradlew test --tests "패키지명.클래스명"
```

## 프로젝트 구조

```
src/main/java/kuit/hackathon/proj_objection/
├── controller/     # REST API 엔드포인트
├── service/        # 비즈니스 로직
├── repository/     # 데이터 접근 계층
├── entity/         # JPA 엔티티
├── dto/            # 데이터 전송 객체
├── exception/      # 예외 클래스
└── config/         # 설정 클래스
```

## 코딩 컨벤션

### 레이어 구조

**Controller -> Service -> Repository -> Entity** 순서로 호출합니다.

### 클래스 어노테이션

```java
// Controller
@RequiredArgsConstructor
@RestController
public class XxxController { }

// Service
@RequiredArgsConstructor
@Service
public class XxxService { }

// Repository
public interface XxxRepository extends JpaRepository<Xxx, Long> { }

// Entity
@Getter
@Entity
public class Xxx extends BaseEntity { }

// DTO
@Getter
@AllArgsConstructor
public class XxxDto { }
```

### 의존성 주입

생성자 주입을 사용합니다. `@RequiredArgsConstructor`와 `final` 필드를 활용합니다.

```java
@RequiredArgsConstructor
@Service
public class LoginService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
}
```

### Entity

- 모든 엔티티는 `BaseEntity`를 상속하여 `createdAt`, `modifiedAt` 필드를 자동 관리합니다.
- 정적 팩토리 메서드 `create()`를 사용하여 객체를 생성합니다.

```java
@Getter
@Entity
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public static User create(String nickname, String password) {
        User user = new User();
        user.nickname = nickname;
        user.password = password;
        return user;
    }
}
```

### API 응답 형식

**성공 응답**: `BaseResponse<T>`

```json
{
  "success": true,
  "code": 200,
  "result": "데이터"
}
```

**에러 응답**: `BaseErrorResponse`

```json
{
  "success": false,
  "code": 400,
  "result": "에러 메시지"
}
```

### 예외 처리

1. `BaseException`을 상속한 커스텀 예외 클래스를 생성합니다.
2. `MainExceptionHandler`에 해당 예외 핸들러를 등록합니다.

```java
// 1. 예외 클래스 생성
public class LoginException extends BaseException {
    public LoginException() {
        super(HttpStatus.BAD_REQUEST, "로그인에 실패했습니다.");
    }
}

// 2. 핸들러 등록
@ExceptionHandler({LoginException.class})
public BaseErrorResponse handle_LoginException(LoginException exception) {
    log.error("MainExceptionHandler.handle_LoginException <{}> {}", exception.getMessage(), exception);
    return BaseErrorResponse.of(exception);
}
```

### 테스트 컨벤션

BDD Mockito 기반의 given-when-then 패턴을 사용합니다.

```java
@ExtendWith(MockitoExtension.class)
class XxxServiceTest {

    @Mock
    private XxxRepository xxxRepository;

    @InjectMocks
    private XxxService xxxService;

    @Test
    @DisplayName("테스트 설명 (한글)")
    void methodName_condition_expectedResult() {
        // given
        given(xxxRepository.findById(1L)).willReturn(Optional.of(xxx));

        // when
        Xxx result = xxxService.find(1L);

        // then
        assertThat(result).isEqualTo(xxx);
        then(xxxRepository).should(times(1)).findById(1L);
    }
}
```

## API 엔드포인트

| Method | Endpoint   | Description         |
|--------|------------|---------------------|
| POST   | /login     | 로그인 (자동 회원가입) |
| POST   | /logout    | 로그아웃             |
| GET    | /test      | 서버 상태 확인        |
| GET    | /test/me   | 로그인 유저 정보 확인  |

## 환경 설정

### 개발 환경

`application.yml`에서 직접 DB 정보를 설정합니다.

### 운영 환경

환경 변수를 사용합니다:
- `MYSQL_URL`
- `MYSQL_USERNAME`
- `MYSQL_PASSWORD`

### Hibernate DDL

- `create`: 개발 초기 (테이블 재생성)
- `update`: 개발 중 (스키마 변경 반영)
- `none`: 운영 환경
