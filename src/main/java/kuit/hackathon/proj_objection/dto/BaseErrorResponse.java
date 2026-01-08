package kuit.hackathon.proj_objection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kuit.hackathon.proj_objection.exception.BaseException;
import lombok.Getter;

@Schema(description = "API 에러 응답")
@Getter
public class BaseErrorResponse {

    @Schema(description = "요청 성공 여부", example = "false")
    private boolean success;

    @Schema(description = "에러 코드", example = "400")
    private int code;

    @Schema(description = "에러 메시지", example = "로그인에 실패했습니다.")
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
