package kuit.hackathon.proj_objection.client.openai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "토큰 사용량")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Usage {

    @Schema(description = "프롬프트 토큰 수")
    @JsonProperty("prompt_tokens")
    private Integer promptTokens;

    @Schema(description = "완성 토큰 수")
    @JsonProperty("completion_tokens")
    private Integer completionTokens;

    @Schema(description = "총 토큰 수")
    @JsonProperty("total_tokens")
    private Integer totalTokens;
}
