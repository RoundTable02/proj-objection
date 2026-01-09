package kuit.hackathon.proj_objection.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class FinalJudgement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", unique = true, nullable = false)
    private ChatRoom chatRoom;

    @Column(nullable = false)
    private String winner;

    @Column(nullable = false)
    private String plaintiff;

    @Column(nullable = false)
    private String defendant;

    @Column(nullable = false)
    private Integer winnerLogicScore;

    @Column(nullable = false)
    private Integer winnerEmpathyScore;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String judgmentComment;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String winnerReason;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String loserReason;

    public static FinalJudgement create(
            ChatRoom chatRoom,
            String winner,
            String plaintiff,
            String defendant,
            Integer winnerLogicScore,
            Integer winnerEmpathyScore,
            String judgmentComment,
            String winnerReason,
            String loserReason
    ) {
        FinalJudgement judgement = new FinalJudgement();
        judgement.chatRoom = chatRoom;
        judgement.winner = winner;
        judgement.plaintiff = plaintiff;
        judgement.defendant = defendant;
        judgement.winnerLogicScore = winnerLogicScore;
        judgement.winnerEmpathyScore = winnerEmpathyScore;
        judgement.judgmentComment = judgmentComment;
        judgement.winnerReason = winnerReason;
        judgement.loserReason = loserReason;
        return judgement;
    }
}
