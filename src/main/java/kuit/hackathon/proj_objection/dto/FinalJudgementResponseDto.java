package kuit.hackathon.proj_objection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kuit.hackathon.proj_objection.entity.FinalJudgement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "최종 판결문 조회 응답")
@Getter
@Builder
@AllArgsConstructor
public class FinalJudgementResponseDto {

    @Schema(description = "판결문 ID", example = "1")
    private Long id;

    @Schema(description = "채팅방 ID", example = "1")
    private Long chatRoomId;

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

    public static FinalJudgementResponseDto from(FinalJudgement judgement) {
        return FinalJudgementResponseDto.builder()
                .id(judgement.getId())
                .chatRoomId(judgement.getChatRoom().getId())
                .winner(judgement.getWinner())
                .plaintiff(judgement.getPlaintiff())
                .defendant(judgement.getDefendant())
                .winnerLogicScore(judgement.getWinnerLogicScore())
                .winnerEmpathyScore(judgement.getWinnerEmpathyScore())
                .judgmentComment(judgement.getJudgmentComment())
                .winnerReason(judgement.getWinnerReason())
                .loserReason(judgement.getLoserReason())
                .build();
    }
}
