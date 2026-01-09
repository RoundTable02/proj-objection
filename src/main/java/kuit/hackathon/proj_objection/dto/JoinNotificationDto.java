package kuit.hackathon.proj_objection.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JoinNotificationDto {
    private String type; // "USER_JOINED"
    private String nickname; // 입장한 사람
    private String role; // "PARTICIPANT" or "OBSERVER"
    private String message; // "철수님이 대화 상대방으로 입장했습니다."
}