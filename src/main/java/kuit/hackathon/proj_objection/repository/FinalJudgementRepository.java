package kuit.hackathon.proj_objection.repository;

import kuit.hackathon.proj_objection.entity.FinalJudgement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FinalJudgementRepository extends JpaRepository<FinalJudgement, Long> {

    // chatRoom.id로 FinalJudgement 조회
    Optional<FinalJudgement> findByChatRoom_Id(Long chatRoomId);

    // 해당 채팅방에 이미 판결문이 존재하는지 확인
    boolean existsByChatRoom_Id(Long chatRoomId);
}
