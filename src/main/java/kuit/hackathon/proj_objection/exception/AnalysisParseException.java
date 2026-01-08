package kuit.hackathon.proj_objection.exception;

import org.springframework.http.HttpStatus;

public class AnalysisParseException extends BaseException {
    public AnalysisParseException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "AI 응답을 파싱하는 데 실패했습니다.");
    }

    public AnalysisParseException(String detail) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "AI 응답 파싱 실패: " + detail);
    }
}
