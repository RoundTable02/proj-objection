package kuit.hackathon.proj_objection.client.openai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "OpenAI Chat Completion 요청")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatCompletionRequest {

    @Schema(description = "모델명", example = "gpt-4o")
    private String model;

    @Schema(description = "메시지 목록")
    private List<Message> messages;

    @Schema(description = "온도 (창의성)", example = "0.7")
    private Double temperature;

    @Schema(description = "최대 토큰 수", example = "1000")
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    public static ChatCompletionRequest of(String model, String userMessage) {
        return ChatCompletionRequest.builder()
                .model(model)
                .messages(List.of(Message.user(userMessage)))
                .build();
    }

    public static ChatCompletionRequest of(String model, List<Message> messages) {
        return ChatCompletionRequest.builder()
                .model(model)
                .messages(messages)
                .build();
    }
}
