package kuit.hackathon.proj_objection.repository;

import kuit.hackathon.proj_objection.entity.ChatMessage;
import kuit.hackathon.proj_objection.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 특정 채팅방의 모든 메시지 조회 (시간 역순)
    List<ChatMessage> findByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom);

    // 특정 채팅방의 모든 메시지 조회 (시간 순서)
    List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);

    // 특정 채팅방에서 lastMessageId 이후의 메시지 조회 (ID 오름차순) - 폴링용
    List<ChatMessage> findByChatRoomAndIdGreaterThanOrderByIdAsc(ChatRoom chatRoom, Long lastMessageId);

    // 특정 채팅방에서 lastMessageId 이후의 메시지 조회 (JOIN FETCH로 N+1 해결)
    @Query("SELECT m FROM ChatMessage m JOIN FETCH m.sender WHERE m.chatRoom = :chatRoom AND m.id > :lastMessageId ORDER BY m.id ASC")
    List<ChatMessage> findMessagesWithSender(@Param("chatRoom") ChatRoom chatRoom, @Param("lastMessageId") Long lastMessageId);
}
