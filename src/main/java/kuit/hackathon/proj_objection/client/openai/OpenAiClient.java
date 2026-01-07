package kuit.hackathon.proj_objection.client.openai;

import kuit.hackathon.proj_objection.client.openai.dto.ChatCompletionRequest;
import kuit.hackathon.proj_objection.client.openai.dto.ChatCompletionResponse;

public interface OpenAiClient {

    /**
     * Chat Completion API 호출
     * @param request 요청 DTO
     * @return 응답 DTO
     */
    ChatCompletionResponse chatCompletion(ChatCompletionRequest request);

    /**
     * 제너릭 POST API 호출 (확장성)
     * @param endpoint API 엔드포인트
     * @param request 요청 객체
     * @param responseType 응답 타입 클래스
     * @return 응답 객체
     */
    <T> T post(String endpoint, Object request, Class<T> responseType);
}
