package kuit.hackathon.proj_objection.exception;

import org.springframework.http.HttpStatus;

// 이게 필요한가..?

public class ChatRoomClosedException extends BaseException {
    public ChatRoomClosedException() {
        super(HttpStatus.BAD_REQUEST, "종료된 채팅방입니다.");
    }
}
