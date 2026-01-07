package kuit.hackathon.proj_objection.client.openai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "Chat Completion 선택지")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Choice {

    @Schema(description = "인덱스")
    private Integer index;

    @Schema(description = "메시지")
    private Message message;

    @Schema(description = "종료 이유")
    @JsonProperty("finish_reason")
    private String finishReason;
}
