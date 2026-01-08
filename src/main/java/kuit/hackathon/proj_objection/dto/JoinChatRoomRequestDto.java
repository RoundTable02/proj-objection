package kuit.hackathon.proj_objection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "채팅방 입장 요청")
@Getter
@AllArgsConstructor
public class JoinChatRoomRequestDto {

    @Schema(description = "초대 코드 (participantCode 또는 observerCode)", example = "1234-5678")
    private String inviteCode;
}
