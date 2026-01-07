package kuit.hackathon.proj_objection.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// 채팅 메세지 조회 응답
// 채팅방의 모든 메시지를 가져옴
@Getter
@AllArgsConstructor
public class ChatMessageListDto {
    private Long messageId;
    private String senderNickname;
    private String content;
    private LocalDateTime createdAt;
    private ChatMessageDto.MessageType type; // ME or OTHER
}
