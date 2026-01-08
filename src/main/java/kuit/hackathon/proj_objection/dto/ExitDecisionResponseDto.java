package kuit.hackathon.proj_objection.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExitDecisionResponseDto {
    private Long chatRoomId;
    private Boolean approved;
    private String message;
}
