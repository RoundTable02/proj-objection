package kuit.hackathon.proj_objection.exception;

import org.springframework.http.HttpStatus;

public class InvalidInviteCodeException extends BaseException {
    public InvalidInviteCodeException() {
        super(HttpStatus.BAD_REQUEST, "유효하지 않은 초대 코드입니다.");
    }
}
