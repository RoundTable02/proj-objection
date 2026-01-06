package kuit.hackathon.proj_objection.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginRequestDto {
    private String nickname;
    private String password;
}
