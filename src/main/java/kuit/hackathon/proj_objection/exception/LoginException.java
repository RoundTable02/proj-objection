package kuit.hackathon.proj_objection.exception;

import org.springframework.http.HttpStatus;

public class LoginException extends BaseException {
    public LoginException() {
        super(HttpStatus.BAD_REQUEST, "로그인에 실패했습니다.");
    }
}
