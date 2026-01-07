package kuit.hackathon.proj_objection.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public class OpenAiApiException extends BaseException {

    private final HttpStatusCode statusCode;

    public OpenAiApiException(HttpStatusCode statusCode, String message) {
        super(HttpStatus.valueOf(statusCode.value()), "OpenAI API 호출 실패: " + message);
        this.statusCode = statusCode;
    }

    public OpenAiApiException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "OpenAI API 호출 실패: " + message);
        this.statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
