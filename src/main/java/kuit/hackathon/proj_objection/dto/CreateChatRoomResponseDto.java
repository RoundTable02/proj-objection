package kuit.hackathon.proj_objection.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateChatRoomResponseDto {
    private Long chatRoomId;
    private String title;
    private String participantCode; // 대화 상대방 초대 코드
    private String observerCode;    // 관전자 초대 코드
}
