package kuit.hackathon.proj_objection.client.openai;

import kuit.hackathon.proj_objection.client.openai.dto.ChatCompletionRequest;
import kuit.hackathon.proj_objection.client.openai.dto.ChatCompletionResponse;
import kuit.hackathon.proj_objection.exception.OpenAiApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@RequiredArgsConstructor
@Component
public class OpenAiClientImpl implements OpenAiClient {

    private final RestClient restClient;

    @Override
    public ChatCompletionResponse chatCompletion(ChatCompletionRequest request) {
        return post("/v1/chat/completions", request, ChatCompletionResponse.class);
    }

    @Override
    public <T> T post(String endpoint, Object request, Class<T> responseType) {
        return restClient.post()
                .uri(endpoint)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    log.error("OpenAI API 4xx 에러: {} - {}", res.getStatusCode(), endpoint);
                    throw new OpenAiApiException(res.getStatusCode(), "클라이언트 에러");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    log.error("OpenAI API 5xx 에러: {} - {}", res.getStatusCode(), endpoint);
                    throw new OpenAiApiException(res.getStatusCode(), "서버 에러");
                })
                .body(responseType);
    }
}
