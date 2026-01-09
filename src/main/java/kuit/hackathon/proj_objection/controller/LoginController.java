package kuit.hackathon.proj_objection.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kuit.hackathon.proj_objection.dto.response.BaseErrorResponse;
import kuit.hackathon.proj_objection.dto.response.BaseResponse;
import kuit.hackathon.proj_objection.dto.request.LoginRequestDto;
import kuit.hackathon.proj_objection.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "로그인/로그아웃 API")
@RequiredArgsConstructor
@RestController
public class LoginController {
    private final LoginService loginService;

    @Operation(summary = "로그인", description = "닉네임과 비밀번호로 로그인합니다. 신규 사용자는 자동으로 회원가입됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "로그인 실패 (비밀번호 불일치)",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class)))
    })
    @PostMapping("/login")
    public BaseResponse<String> login(
            @RequestBody LoginRequestDto loginRequestDto,
            HttpServletRequest httpRequest
    ) {
        Long userId = loginService.login(
                loginRequestDto.getNickname(),
                loginRequestDto.getPassword()
        );

        // 기존 세션이 있으면 무효화 (세션 고정 공격 방지)
        HttpSession existingSession = httpRequest.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }

        // 새 세션 생성 후 사용자 정보 저장
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute("userId", userId);

        return new BaseResponse<>("Login successful");
    }

    @Operation(summary = "로그아웃", description = "현재 세션을 종료하고 로그아웃합니다.")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @PostMapping("/logout")
    public BaseResponse<String> logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return new BaseResponse<>("Logout successful");
    }
}
