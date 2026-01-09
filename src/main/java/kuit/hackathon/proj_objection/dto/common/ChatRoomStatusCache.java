package kuit.hackathon.proj_objection.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import kuit.hackathon.proj_objection.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "채팅방 상태 캐시 데이터")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomStatusCache {

    @Schema(description = "채팅방 상태", example = "ALIVE")
    private ChatRoom.RoomStatus status;

    @Schema(description = "종료 요청자 닉네임", example = "홍길동", nullable = true)
    private String finishRequestNickname;
}
