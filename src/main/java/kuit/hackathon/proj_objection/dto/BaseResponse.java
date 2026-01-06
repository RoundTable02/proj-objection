package kuit.hackathon.proj_objection.dto;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BaseResponse<T> {
    private boolean success;
    private int code;
    private T result;

    public BaseResponse(T result) {
        this.success = true;
        this.code = HttpStatus.OK.value();
        this.result = result;
    }
}
