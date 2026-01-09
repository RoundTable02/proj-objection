package kuit.hackathon.proj_objection.repository;

import kuit.hackathon.proj_objection.entity.ChatMessage;
import kuit.hackathon.proj_objection.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 특정 채팅방의 모든 메시지 조회 (시간 역순)
    List<ChatMessage> findByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom);

    // 특정 채팅방의 모든 메시지 조회 (시간 순서)
    List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);

}
