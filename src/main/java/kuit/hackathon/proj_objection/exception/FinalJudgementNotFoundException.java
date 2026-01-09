package kuit.hackathon.proj_objection.exception;

import org.springframework.http.HttpStatus;

public class FinalJudgementNotFoundException extends BaseException {
    public FinalJudgementNotFoundException() {
        super(HttpStatus.NOT_FOUND, "최종 판결문을 찾을 수 없습니다.");
    }

    public FinalJudgementNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
