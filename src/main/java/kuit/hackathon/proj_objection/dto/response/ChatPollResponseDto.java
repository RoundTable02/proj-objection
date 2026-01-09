package kuit.hackathon.proj_objection.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kuit.hackathon.proj_objection.dto.common.ChatPollMessageDto;
import kuit.hackathon.proj_objection.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Schema(description = "채팅 폴링 응답")
@Getter
@Builder
@AllArgsConstructor
public class ChatPollResponseDto {

    @Schema(description = "새로운 메시지 목록")
    private List<ChatPollMessageDto> messages;

    @Schema(description = "채팅방 상태", example = "ALIVE")
    private ChatRoom.RoomStatus chatRoomStatus;

    @Schema(description = "종료 요청을 보낸 사용자 닉네임", example = "홍길동", nullable = true)
    private String finishRequestNickname;

    @Schema(description = "참여자별 승률 (닉네임 → 점수)", example = "{\"철수\": 73, \"영희\": 27}")
    private Map<String, Integer> percent;
}
