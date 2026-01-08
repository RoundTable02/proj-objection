package kuit.hackathon.proj_objection.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExitNotificationDto {
    private String type; // EXIT_REQUEST, EXIT_APPROVED, EXIT_REJECTED
    private String requesterNickname;
    private String message;
}
