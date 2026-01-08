package kuit.hackathon.proj_objection.client.openai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "OpenAI Chat Completion 응답")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatCompletionResponse {

    @Schema(description = "응답 ID")
    private String id;

    @Schema(description = "객체 타입")
    private String object;

    @Schema(description = "생성 시간")
    private Long created;

    @Schema(description = "사용된 모델")
    private String model;

    @Schema(description = "선택지 목록")
    private List<Choice> choices;

    @Schema(description = "토큰 사용량")
    private Usage usage;

    /**
     * 첫 번째 응답의 내용을 반환합니다.
     * @return 응답 내용, choices가 비어있으면 null
     */
    public String getContent() {
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        Message message = choices.get(0).getMessage();
        return message != null ? message.getContent() : null;
    }
}
