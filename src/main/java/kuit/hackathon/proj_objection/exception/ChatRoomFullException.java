package kuit.hackathon.proj_objection.exception;

import org.springframework.http.HttpStatus;

public class ChatRoomFullException extends BaseException {
    public ChatRoomFullException() {
        super(HttpStatus.BAD_REQUEST, "이미 활성화된 채팅방입니다.");
    }
}
