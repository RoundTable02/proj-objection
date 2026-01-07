package kuit.hackathon.proj_objection.exception;

import org.springframework.http.HttpStatus;

public class MessageSendPermissionDeniedException extends BaseException {
    public MessageSendPermissionDeniedException() {
        super(HttpStatus.FORBIDDEN, "메시지를 전송할 권한이 없습니다. 관전자는 메시지를 전송할 수 없습니다.");
    }
}
