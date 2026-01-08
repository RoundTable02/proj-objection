package kuit.hackathon.proj_objection.client.openai;

import kuit.hackathon.proj_objection.client.openai.dto.ChatCompletionRequest;
import kuit.hackathon.proj_objection.client.openai.dto.ChatCompletionResponse;
import kuit.hackathon.proj_objection.client.openai.dto.Choice;
import kuit.hackathon.proj_objection.client.openai.dto.Message;
import kuit.hackathon.proj_objection.exception.OpenAiApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class OpenAiClientImplTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private OpenAiClientImpl openAiClient;

    @BeforeEach
    void setUp() {
        openAiClient = new OpenAiClientImpl(restClient);
    }

    @Test
    @DisplayName("chatCompletion 성공 시 응답 반환")
    void chatCompletion_success_shouldReturnResponse() {
        // given
        ChatCompletionRequest request = ChatCompletionRequest.of("gpt-4o", "Hello");
        ChatCompletionResponse expectedResponse = createMockResponse("Hi there!");

        given(restClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri("/v1/chat/completions")).willReturn(requestBodySpec);
        given(requestBodySpec.body(request)).willReturn(requestBodySpec);
        given(requestBodySpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.onStatus(any(), any())).willReturn(responseSpec);
        given(responseSpec.body(ChatCompletionResponse.class)).willReturn(expectedResponse);

        // when
        ChatCompletionResponse response = openAiClient.chatCompletion(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("Hi there!");
        then(restClient).should(times(1)).post();
    }

    @Test
    @DisplayName("제너릭 post 메서드로 커스텀 엔드포인트 호출")
    void post_withCustomEndpoint_shouldCallCorrectUri() {
        // given
        String endpoint = "/v1/custom/endpoint";
        ChatCompletionRequest request = ChatCompletionRequest.of("gpt-4o", "Test");
        ChatCompletionResponse expectedResponse = createMockResponse("Custom response");

        given(restClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(endpoint)).willReturn(requestBodySpec);
        given(requestBodySpec.body(request)).willReturn(requestBodySpec);
        given(requestBodySpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.onStatus(any(), any())).willReturn(responseSpec);
        given(responseSpec.body(ChatCompletionResponse.class)).willReturn(expectedResponse);

        // when
        ChatCompletionResponse result = openAiClient.post(endpoint, request, ChatCompletionResponse.class);

        // then
        assertThat(result.getContent()).isEqualTo("Custom response");
        then(requestBodyUriSpec).should(times(1)).uri(endpoint);
    }

    @Test
    @DisplayName("시스템 메시지와 사용자 메시지를 함께 전송")
    void chatCompletion_withSystemAndUserMessage_shouldSendBoth() {
        // given
        List<Message> messages = List.of(
                Message.system("You are a helpful assistant."),
                Message.user("Hello")
        );
        ChatCompletionRequest request = ChatCompletionRequest.of("gpt-4o", messages);
        ChatCompletionResponse expectedResponse = createMockResponse("Hello! How can I help?");

        given(restClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri("/v1/chat/completions")).willReturn(requestBodySpec);
        given(requestBodySpec.body(request)).willReturn(requestBodySpec);
        given(requestBodySpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.onStatus(any(), any())).willReturn(responseSpec);
        given(responseSpec.body(ChatCompletionResponse.class)).willReturn(expectedResponse);

        // when
        ChatCompletionResponse response = openAiClient.chatCompletion(request);

        // then
        assertThat(response.getContent()).isEqualTo("Hello! How can I help?");
        assertThat(request.getMessages()).hasSize(2);
        assertThat(request.getMessages().get(0).getRole()).isEqualTo("system");
        assertThat(request.getMessages().get(1).getRole()).isEqualTo("user");
    }

    private ChatCompletionResponse createMockResponse(String content) {
        Message message = Message.assistant(content);
        Choice choice = new Choice(0, message, "stop");
        return new ChatCompletionResponse(
                "chatcmpl-123",
                "chat.completion",
                System.currentTimeMillis() / 1000,
                "gpt-4o",
                List.of(choice),
                null
        );
    }
}
