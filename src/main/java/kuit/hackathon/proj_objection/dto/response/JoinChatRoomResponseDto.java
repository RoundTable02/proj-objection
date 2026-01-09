package kuit.hackathon.proj_objection.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "채팅방 입장 응답")
@Getter
@AllArgsConstructor
public class JoinChatRoomResponseDto {

    @Schema(description = "채팅방 ID", example = "1")
    private Long chatRoomId;

    @Schema(description = "채팅방 제목", example = "2026가단AI01080001")
    private String title;

    @Schema(description = "부여된 역할", example = "PARTICIPANT", allowableValues = {"PARTICIPANT", "OBSERVER"})
    private String role;

    @Schema(description = "입장 결과 메시지", example = "대화 상대방으로 입장했습니다.")
    private String message;
}
