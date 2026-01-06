package kuit.hackathon.proj_objection.dto;

import kuit.hackathon.proj_objection.exception.BaseException;
import lombok.Getter;

@Getter
public class BaseErrorResponse {
    private boolean success;
    private int code;
    private String result;

    private BaseErrorResponse(boolean success, int code, String result) {
        this.success = success;
        this.code = code;
        this.result = result;
    }

    public static BaseErrorResponse of(BaseException e) {
        return new BaseErrorResponse(false, e.getCode(), e.getMessage());
    }
}
