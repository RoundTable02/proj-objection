package kuit.hackathon.proj_objection.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "AI 정밀 분석 결과")
@Getter
@Builder
@AllArgsConstructor
public class AnalysisResult {

    @Schema(description = "승자 닉네임", example = "홍길동")
    private String winner;

    @Schema(description = "승자의 논리력 점수 (0-100)", example = "85")
    private int winnerLogicScore;

    @Schema(description = "승자의 공감력 점수 (0-100)", example = "72")
    private int winnerEmpathyScore;

    @Schema(description = "심판 코멘트", example = "양측 모두 좋은 논쟁을 펼쳤습니다.")
    private String judgmentComment;

    @Schema(description = "승자가 가산점을 받은 이유", example = "논리적인 근거를 잘 제시했습니다.")
    private String winnerReason;

    @Schema(description = "패자가 감점된 이유", example = "감정적인 표현이 많았습니다.")
    private String loserReason;
}
