package kuit.hackathon.proj_objection.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;


// 메세지 응답 (Websocket 브로드캐스트 용)
@Getter
@AllArgsConstructor
public class ChatMessageDto {
    private Long MessageId;
    private Long SenderId;
    private String senderNickName;
    private String content;
    private LocalDateTime createdAt;
    private MessageType type; // ME or OTHER

    public enum MessageType {
        ME,      // 내가 보낸 메시지
        OTHER    // 다른 사람이 보낸 메시지
    }
}

