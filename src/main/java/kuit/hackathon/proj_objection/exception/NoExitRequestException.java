package kuit.hackathon.proj_objection.exception;

import org.springframework.http.HttpStatus;

// 종료 요청이 없음
public class NoExitRequestException extends BaseException {
    public NoExitRequestException() {
        super(HttpStatus.BAD_REQUEST, "처리할 종료 요청이 없습니다.");
    }
}
