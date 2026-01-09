package kuit.hackathon.proj_objection.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExitRequestResponseDto {
    private Long chatRoomId;
    private String requesterNickname;
    private String message;
}
