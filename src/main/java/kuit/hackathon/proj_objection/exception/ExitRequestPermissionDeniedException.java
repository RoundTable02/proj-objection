package kuit.hackathon.proj_objection.exception;

import org.springframework.http.HttpStatus;

public class ExitRequestPermissionDeniedException extends BaseException {
    public ExitRequestPermissionDeniedException() {
        super(HttpStatus.FORBIDDEN, "PARTICIPANT만 종료를 요청할 수 있습니다.");
    }
}
