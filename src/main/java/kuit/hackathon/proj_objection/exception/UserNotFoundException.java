package kuit.hackathon.proj_objection.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BaseException {
    public UserNotFoundException() {
        super(HttpStatus.BAD_REQUEST, "사용자 정보가 존재하지 않습니다.");
    }

    public UserNotFoundException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
