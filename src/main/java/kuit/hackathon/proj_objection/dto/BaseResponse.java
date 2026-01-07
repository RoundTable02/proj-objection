package kuit.hackathon.proj_objection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Schema(description = "API 성공 응답")
@Getter
public class BaseResponse<T> {

    @Schema(description = "요청 성공 여부", example = "true")
    private boolean success;

    @Schema(description = "HTTP 상태 코드", example = "200")
    private int code;

    @Schema(description = "응답 데이터")
    private T result;

    public BaseResponse(T result) {
        this.success = true;
        this.code = HttpStatus.OK.value();
        this.result = result;
    }
}
