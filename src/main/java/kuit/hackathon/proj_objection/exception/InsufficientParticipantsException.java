package kuit.hackathon.proj_objection.exception;

import org.springframework.http.HttpStatus;

public class InsufficientParticipantsException extends BaseException {
    public InsufficientParticipantsException() {
        super(HttpStatus.BAD_REQUEST, "분석을 위해서는 최소 2명의 대화 참여자가 필요합니다.");
    }

    public InsufficientParticipantsException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
