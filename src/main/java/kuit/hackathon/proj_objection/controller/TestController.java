package kuit.hackathon.proj_objection.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kuit.hackathon.proj_objection.annotation.LoginUser;
import kuit.hackathon.proj_objection.dto.BaseErrorResponse;
import kuit.hackathon.proj_objection.dto.BaseResponse;
import kuit.hackathon.proj_objection.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "테스트", description = "서버 상태 및 세션 확인 API")
@RestController
@RequestMapping("/test")
public class TestController {

    @Operation(summary = "서버 상태 확인", description = "서버가 정상 동작 중인지 확인합니다.")
    @ApiResponse(responseCode = "200", description = "서버 정상 동작")
    @GetMapping
    public BaseResponse<String> test() {
        return new BaseResponse<>("test successful");
    }

    @Operation(summary = "로그인 사용자 정보 조회", description = "현재 로그인된 사용자의 닉네임을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "로그인되지 않음",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class)))
    })
    @GetMapping("/me")
    public BaseResponse<String> testSession(
            @Parameter(hidden = true) @LoginUser User user
    ) {
        return new BaseResponse<>(user.getNickname());
    }
}
