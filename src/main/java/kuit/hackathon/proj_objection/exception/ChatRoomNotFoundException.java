package kuit.hackathon.proj_objection.exception;

import org.springframework.http.HttpStatus;

public class ChatRoomNotFoundException extends BaseException {
    public ChatRoomNotFoundException() {
        super(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다.");
    }

    public ChatRoomNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}

