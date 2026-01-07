package kuit.hackathon.proj_objection.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JoinChatRoomResponseDto {
    private Long chatRoomId;
    private String title;
    private String role;
    private String message;
}
