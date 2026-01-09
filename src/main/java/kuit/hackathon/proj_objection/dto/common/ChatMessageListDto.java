package kuit.hackathon.proj_objection.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "채팅 메시지 조회 응답")
@Getter
@AllArgsConstructor
public class ChatMessageListDto {

    @Schema(description = "메시지 ID", example = "1")
    private Long messageId;

    @Schema(description = "발신자 닉네임", example = "홍길동")
    private String senderNickname;

    @Schema(description = "메시지 내용", example = "안녕하세요!")
    private String content;

    @Schema(description = "메시지 생성 시간", example = "2026-01-08T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "메시지 타입 (ME: 내가 보낸 메시지, OTHER: 다른 사람이 보낸 메시지)", example = "OTHER")
    private ChatMessageDto.MessageType type;
}
