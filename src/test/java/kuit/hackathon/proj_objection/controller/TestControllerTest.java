package kuit.hackathon.proj_objection.controller;

import kuit.hackathon.proj_objection.client.openai.OpenAiClient;
import kuit.hackathon.proj_objection.client.openai.dto.ChatCompletionRequest;
import kuit.hackathon.proj_objection.client.openai.dto.ChatCompletionResponse;
import kuit.hackathon.proj_objection.client.openai.dto.Choice;
import kuit.hackathon.proj_objection.client.openai.dto.Message;
import kuit.hackathon.proj_objection.dto.BaseResponse;
import kuit.hackathon.proj_objection.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TestControllerTest {

    @Mock
    private OpenAiClient openAiClient;

    @InjectMocks
    private TestController testController;

    @Test
    @DisplayName("test 엔드포인트 호출 시 성공 응답 반환")
    void test_shouldReturnSuccessResponse() {
        // when
        BaseResponse<String> response = testController.test();

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult()).isEqualTo("test successful");
    }

    @Test
    @DisplayName("testSession 호출 시 유저의 닉네임 반환")
    void testSession_withUser_shouldReturnNickname() {
        // given
        String nickname = "testUser";
        User user = User.create(nickname, "encodedPassword");

        // when
        BaseResponse<String> response = testController.testSession(user);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult()).isEqualTo(nickname);
    }

//    @Test
//    @DisplayName("OpenAI 단순 요청 시 응답 반환")
//    void testOpenAi_shouldReturnAiResponse() {
//        // given
//        String userMessage = "안녕하세요?";
//        String expectedResponse = "안녕하세요! 무엇을 도와드릴까요?";
//        ChatCompletionResponse mockResponse = createMockResponse(expectedResponse);
//
//        given(openAiClient.chatCompletion(any(ChatCompletionRequest.class)))
//                .willReturn(mockResponse);
//
//        // when
//        BaseResponse<String> response = testController.testOpenAi(userMessage);
//
//        // then
//        assertThat(response.isSuccess()).isTrue();
//        assertThat(response.getResult()).isEqualTo(expectedResponse);
//    }
//
//    @Test
//    @DisplayName("OpenAI 시스템 프롬프트 포함 요청 시 응답 반환")
//    void testOpenAiWithSystem_shouldReturnAiResponse() {
//        // given
//        String systemPrompt = "당신은 친절한 AI 어시스턴트입니다.";
//        String userMessage = "안녕하세요?";
//        String expectedResponse = "안녕하세요! 저는 친절한 AI 어시스턴트입니다.";
//        ChatCompletionResponse mockResponse = createMockResponse(expectedResponse);
//
//        given(openAiClient.chatCompletion(any(ChatCompletionRequest.class)))
//                .willReturn(mockResponse);
//
//        TestController.OpenAiTestRequest request = new TestController.OpenAiTestRequest(
//                systemPrompt, userMessage
//        );
//
//        // when
//        BaseResponse<String> response = testController.testOpenAiWithSystem(request);
//
//        // then
//        assertThat(response.isSuccess()).isTrue();
//        assertThat(response.getResult()).isEqualTo(expectedResponse);
//    }

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
