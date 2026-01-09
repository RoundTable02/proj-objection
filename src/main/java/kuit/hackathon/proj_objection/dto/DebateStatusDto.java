package kuit.hackathon.proj_objection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Schema(description = "토론 현황 (실시간 승률)")
@Getter
@AllArgsConstructor
public class DebateStatusDto {

    @Schema(description = "메시지 타입", example = "DEBATE_STATUS")
    private String type;

    @Schema(description = "참여자별 점수 (닉네임: 점수)", example = "{\"홍길동\": 57, \"김철수\": 43}")
    private Map<String, Integer> score;

    public static DebateStatusDto of(Map<String, Integer> score) {
        return new DebateStatusDto("DEBATE_STATUS", score);
    }
}
