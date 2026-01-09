package kuit.hackathon.proj_objection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "AI 판결 분석 결과 알림")
@Getter
@Builder
@AllArgsConstructor
public class JudgmentNotificationDto {

    @Schema(description = "알림 타입 (FINAL_JUDGMENT: 판결 완료, JUDGMENT_ERROR: 분석 실패)", example = "FINAL_JUDGMENT")
    private String type;

    @Schema(description = "승자 닉네임", example = "홍길동")
    private String winner;

    @Schema(description = "원고 닉네임 (채팅방 생성자)", example = "홍길동")
    private String plaintiff;

    @Schema(description = "피고 닉네임 (상대방)", example = "김철수")
    private String defendant;

    @Schema(description = "승자의 논리력 점수 (0-100)", example = "85")
    private Integer winnerLogicScore;

    @Schema(description = "승자의 공감력 점수 (0-100)", example = "72")
    private Integer winnerEmpathyScore;

    @Schema(description = "심판 코멘트", example = "원고가 구체적인 근거를 제시하며 논리적으로 주장을 펼쳤습니다.")
    private String judgmentComment;

    @Schema(description = "승자가 가산점을 받은 이유", example = "구체적 사례와 논리적 근거 제시")
    private String winnerReason;

    @Schema(description = "패자가 감점된 이유", example = "감정적 대응으로 일관")
    private String loserReason;

    @Schema(description = "에러 메시지 (JUDGMENT_ERROR 타입일 때만 사용)", example = "AI 분석 중 오류가 발생했습니다.")
    private String errorMessage;
}
