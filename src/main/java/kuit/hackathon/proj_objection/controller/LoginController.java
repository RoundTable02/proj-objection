package kuit.hackathon.proj_objection.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kuit.hackathon.proj_objection.dto.BaseResponse;
import kuit.hackathon.proj_objection.dto.LoginRequestDto;
import kuit.hackathon.proj_objection.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@CrossOrigin(originPatterns = "*", allowCredentials = "true")      // CORS
public class LoginController {
    private final LoginService loginService;

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

    @PostMapping("/logout")
    public BaseResponse<String> logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return new BaseResponse<>("Logout successful");
    }
}
