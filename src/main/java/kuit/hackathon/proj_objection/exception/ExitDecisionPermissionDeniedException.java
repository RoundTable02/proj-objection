package kuit.hackathon.proj_objection.exception;

import org.springframework.http.HttpStatus;

// 종료 처리 권한 없음
public class ExitDecisionPermissionDeniedException extends BaseException {
    public ExitDecisionPermissionDeniedException() {
        super(HttpStatus.FORBIDDEN, "종료 요청을 처리할 권한이 없습니다.");
    }
}
