package kuit.hackathon.proj_objection.repository;

import kuit.hackathon.proj_objection.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // participantCode로 채팅방 찾기
    Optional<ChatRoom> findByParticipantCode(String participantCode);

    // observerCode로 채팅방 찾기
    Optional<ChatRoom> findByObserverCode(String observerCode);

    // 초대 코드(둘 중 하나)로 채팅방 찾기
    default Optional<ChatRoom> findByInviteCode(String inviteCode) {
        return findByParticipantCode(inviteCode)
                .or(() -> findByObserverCode(inviteCode));
    }
}
