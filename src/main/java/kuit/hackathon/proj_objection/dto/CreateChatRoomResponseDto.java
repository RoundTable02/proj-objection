package kuit.hackathon.proj_objection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "채팅방 생성 응답")
@Getter
@AllArgsConstructor
public class CreateChatRoomResponseDto {

    @Schema(description = "채팅방 ID", example = "1")
    private Long chatRoomId;

    @Schema(description = "채팅방 제목", example = "2026가단AI01080001")
    private String title;

    @Schema(description = "대화 상대방 초대 코드", example = "1234-5678")
    private String participantCode;

    @Schema(description = "관전자 초대 코드", example = "8765-4321")
    private String observerCode;
}
