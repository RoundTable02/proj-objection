package kuit.hackathon.proj_objection.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExitDecisionRequestDto {
    private Boolean approve; // true: 수락, false: 거절
}
