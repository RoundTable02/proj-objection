package kuit.hackathon.proj_objection.exception;

import org.springframework.http.HttpStatus;

public class AlreadyJoinedChatRoomException extends BaseException {
    public AlreadyJoinedChatRoomException() {
        super(HttpStatus.BAD_REQUEST, "이미 입장한 채팅방입니다.");
    }
}