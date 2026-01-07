package kuit.hackathon.proj_objection.exception;

import org.springframework.http.HttpStatus;

public class ChatRoomMemberNotFoundException extends BaseException {
    public ChatRoomMemberNotFoundException() {
        super(HttpStatus.NOT_FOUND, "채팅방 멤버 정보를 찾을 수 없습니다.");
    }
}
