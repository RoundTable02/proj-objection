package kuit.hackathon.proj_objection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "채팅방 생성 요청")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateChatRoomRequestDto {
    @Schema(description = "승리 보상", example = "커피 한 잔 사기")
    private String reward;
}
