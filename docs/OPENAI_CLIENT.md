# OpenAI Client 사용 가이드

## 개요

`OpenAiClient`는 OpenAI Chat Completion API를 호출하기 위한 Spring Bean입니다. 다른 서비스에서 주입받아 간단하게 사용할 수 있습니다.

## 설정

### 환경변수 설정

```bash
export OPENAI_API_KEY=your-api-key-here
```

### application.yml

```yaml
openai:
  api:
    key: ${OPENAI_API_KEY}
    base-url: https://api.openai.com
    timeout: 30000
    default-model: gpt-4o
```

## 기본 사용법

### 1. 서비스에 주입

```java
@RequiredArgsConstructor
@Service
public class MyService {
    private final OpenAiClient openAiClient;
}
```

### 2. 간단한 질문

```java
public String ask(String question) {
    ChatCompletionRequest request = ChatCompletionRequest.of("gpt-4o", question);
    ChatCompletionResponse response = openAiClient.chatCompletion(request);
    return response.getContent();
}
```

### 3. 시스템 프롬프트 포함

```java
public String askWithSystemPrompt(String userMessage) {
    ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model("gpt-4o")
            .messages(List.of(
                Message.system("You are a helpful assistant."),
                Message.user(userMessage)
            ))
            .build();
    return openAiClient.chatCompletion(request).getContent();
}
```

### 4. 파라미터 설정

```java
public String askWithParams(String userMessage) {
    ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model("gpt-4o")
            .messages(List.of(Message.user(userMessage)))
            .temperature(0.7)      // 창의성 (0.0 ~ 2.0)
            .maxTokens(1000)       // 최대 응답 토큰 수
            .build();
    return openAiClient.chatCompletion(request).getContent();
}
```

### 5. 대화 컨텍스트 유지

```java
public String chat(List<Message> conversationHistory, String newMessage) {
    List<Message> messages = new ArrayList<>(conversationHistory);
    messages.add(Message.user(newMessage));

    ChatCompletionRequest request = ChatCompletionRequest.of("gpt-4o", messages);
    ChatCompletionResponse response = openAiClient.chatCompletion(request);

    // 응답을 대화 기록에 추가
    messages.add(Message.assistant(response.getContent()));

    return response.getContent();
}
```

## Message 타입

| 메서드 | 역할 | 용도 |
|--------|------|------|
| `Message.system(content)` | system | AI의 행동 방식 정의 |
| `Message.user(content)` | user | 사용자 입력 |
| `Message.assistant(content)` | assistant | AI 응답 (대화 기록용) |

## 응답 처리

### ChatCompletionResponse 구조

```java
ChatCompletionResponse response = openAiClient.chatCompletion(request);

// 응답 내용 (편의 메서드)
String content = response.getContent();

// 상세 정보
String id = response.getId();           // 응답 ID
String model = response.getModel();     // 사용된 모델
Long created = response.getCreated();   // 생성 시간 (Unix timestamp)

// 토큰 사용량
Usage usage = response.getUsage();
int promptTokens = usage.getPromptTokens();
int completionTokens = usage.getCompletionTokens();
int totalTokens = usage.getTotalTokens();
```

## 에러 처리

API 호출 실패 시 `OpenAiApiException`이 발생합니다.

```java
try {
    ChatCompletionResponse response = openAiClient.chatCompletion(request);
    return response.getContent();
} catch (OpenAiApiException e) {
    log.error("OpenAI API 호출 실패: {}", e.getMessage());
    // 에러 처리 로직
    throw e;
}
```

### 에러 종류

| HTTP 상태 | 원인 |
|-----------|------|
| 401 | 잘못된 API 키 |
| 429 | Rate Limit 초과 |
| 400 | 잘못된 요청 |
| 5xx | OpenAI 서버 에러 |

## 제너릭 API 호출

다른 OpenAI 엔드포인트를 호출할 때 사용합니다.

```java
MyCustomResponse response = openAiClient.post(
    "/v1/custom/endpoint",
    myRequest,
    MyCustomResponse.class
);
```

## 전체 예제

```java
@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {

    private final OpenAiClient openAiClient;

    public String generateResponse(String userInput) {
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o")
                .messages(List.of(
                    Message.system("당신은 친절한 한국어 AI 어시스턴트입니다."),
                    Message.user(userInput)
                ))
                .temperature(0.7)
                .maxTokens(2000)
                .build();

        try {
            ChatCompletionResponse response = openAiClient.chatCompletion(request);
            log.info("토큰 사용량: {}", response.getUsage().getTotalTokens());
            return response.getContent();
        } catch (OpenAiApiException e) {
            log.error("AI 응답 생성 실패", e);
            return "죄송합니다. 일시적인 오류가 발생했습니다.";
        }
    }
}
```

## 모델 선택 가이드

| 모델 | 특징 | 권장 용도 |
|------|------|----------|
| `gpt-4o` | 최신 고성능 모델 | 복잡한 추론, 고품질 응답 |
| `gpt-4o-mini` | 빠르고 저렴 | 일반적인 대화, 간단한 작업 |
| `gpt-3.5-turbo` | 레거시, 가장 저렴 | 비용 최적화가 중요한 경우 |
