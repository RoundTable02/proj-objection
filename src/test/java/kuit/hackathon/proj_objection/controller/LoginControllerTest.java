package kuit.hackathon.proj_objection.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kuit.hackathon.proj_objection.dto.response.BaseResponse;
import kuit.hackathon.proj_objection.dto.request.LoginRequestDto;
import kuit.hackathon.proj_objection.service.LoginService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock
    private LoginService loginService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpSession existingSession;

    @Mock
    private HttpSession newSession;

    @InjectMocks
    private LoginController loginController;

    @Test
    @DisplayName("로그인 성공 시 기존 세션 무효화 후 새 세션 생성")
    void login_withExistingSession_shouldInvalidateAndCreateNewSession() {
        // given
        String nickname = "testUser";
        String password = "password123";
        Long userId = 1L;
        LoginRequestDto loginRequestDto = new LoginRequestDto(nickname, password);

        given(loginService.login(nickname, password)).willReturn(userId);
        given(httpServletRequest.getSession(false)).willReturn(existingSession);
        given(httpServletRequest.getSession(true)).willReturn(newSession);

        // when
        BaseResponse<String> response = loginController.login(loginRequestDto, httpServletRequest);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult()).isEqualTo("Login successful");

        then(existingSession).should(times(1)).invalidate();
        then(newSession).should(times(1)).setAttribute("userId", userId);
    }

    @Test
    @DisplayName("로그인 성공 시 기존 세션 없으면 새 세션만 생성")
    void login_withoutExistingSession_shouldCreateNewSession() {
        // given
        String nickname = "newUser";
        String password = "password456";
        Long userId = 2L;
        LoginRequestDto loginRequestDto = new LoginRequestDto(nickname, password);

        given(loginService.login(nickname, password)).willReturn(userId);
        given(httpServletRequest.getSession(false)).willReturn(null);
        given(httpServletRequest.getSession(true)).willReturn(newSession);

        // when
        BaseResponse<String> response = loginController.login(loginRequestDto, httpServletRequest);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult()).isEqualTo("Login successful");

        then(existingSession).should(times(0)).invalidate();
        then(newSession).should(times(1)).setAttribute("userId", userId);
    }

    @Test
    @DisplayName("로그아웃 시 세션 존재하면 무효화")
    void logout_withSession_shouldInvalidateSession() {
        // given
        given(httpServletRequest.getSession(false)).willReturn(existingSession);

        // when
        BaseResponse<String> response = loginController.logout(httpServletRequest);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult()).isEqualTo("Logout successful");

        then(existingSession).should(times(1)).invalidate();
    }

    @Test
    @DisplayName("로그아웃 시 세션 없으면 그냥 성공 응답 반환")
    void logout_withoutSession_shouldReturnSuccessResponse() {
        // given
        given(httpServletRequest.getSession(false)).willReturn(null);

        // when
        BaseResponse<String> response = loginController.logout(httpServletRequest);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult()).isEqualTo("Logout successful");
    }
}
