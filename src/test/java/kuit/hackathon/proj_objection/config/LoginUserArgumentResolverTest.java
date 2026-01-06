package kuit.hackathon.proj_objection.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kuit.hackathon.proj_objection.annotation.LoginUser;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.exception.UserNotFoundException;
import kuit.hackathon.proj_objection.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class LoginUserArgumentResolverTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoginUserArgumentResolver resolver;

    @Test
    @DisplayName("@LoginUser User 파라미터를 지원한다")
    void supportsParameter_withLoginUserAndUserType_shouldReturnTrue() throws NoSuchMethodException {
        // given
        MethodParameter parameter = new MethodParameter(
                TestClass.class.getMethod("validMethod", User.class), 0);

        // when
        boolean result = resolver.supportsParameter(parameter);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("@LoginUser 없으면 지원하지 않는다")
    void supportsParameter_withoutLoginUser_shouldReturnFalse() throws NoSuchMethodException {
        // given
        MethodParameter parameter = new MethodParameter(
                TestClass.class.getMethod("noAnnotationMethod", User.class), 0);

        // when
        boolean result = resolver.supportsParameter(parameter);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("User 타입이 아니면 지원하지 않는다")
    void supportsParameter_withWrongType_shouldReturnFalse() throws NoSuchMethodException {
        // given
        MethodParameter parameter = new MethodParameter(
                TestClass.class.getMethod("wrongTypeMethod", String.class), 0);

        // when
        boolean result = resolver.supportsParameter(parameter);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("로그인된 유저 정보를 반환한다")
    void resolveArgument_withLoggedInUser_shouldReturnUser() {
        // given
        Long userId = 1L;
        User user = User.create("testUser", "password");

        NativeWebRequest webRequest = mock(NativeWebRequest.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);

        given(webRequest.getNativeRequest()).willReturn(request);
        given(request.getSession(false)).willReturn(session);
        given(session.getAttribute("userId")).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        Object result = resolver.resolveArgument(null, null, webRequest, null);

        // then
        assertThat(result).isEqualTo(user);
    }

    @Test
    @DisplayName("세션이 없으면 UserNotFoundException 발생")
    void resolveArgument_withoutSession_shouldThrowException() {
        // given
        NativeWebRequest webRequest = mock(NativeWebRequest.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        given(webRequest.getNativeRequest()).willReturn(request);
        given(request.getSession(false)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> resolver.resolveArgument(null, null, webRequest, null))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not logged in");
    }

    @Test
    @DisplayName("세션에 userId 없으면 UserNotFoundException 발생")
    void resolveArgument_withoutUserId_shouldThrowException() {
        // given
        NativeWebRequest webRequest = mock(NativeWebRequest.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);

        given(webRequest.getNativeRequest()).willReturn(request);
        given(request.getSession(false)).willReturn(session);
        given(session.getAttribute("userId")).willReturn(null);

        // when & then
        assertThatThrownBy(() -> resolver.resolveArgument(null, null, webRequest, null))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not logged in");
    }

    @Test
    @DisplayName("DB에 유저 없으면 UserNotFoundException 발생")
    void resolveArgument_withInvalidUserId_shouldThrowException() {
        // given
        Long invalidUserId = 999L;

        NativeWebRequest webRequest = mock(NativeWebRequest.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);

        given(webRequest.getNativeRequest()).willReturn(request);
        given(request.getSession(false)).willReturn(session);
        given(session.getAttribute("userId")).willReturn(invalidUserId);
        given(userRepository.findById(invalidUserId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> resolver.resolveArgument(null, null, webRequest, null))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with ID: " + invalidUserId);
    }

    static class TestClass {
        public void validMethod(@LoginUser User user) {}
        public void noAnnotationMethod(User user) {}
        public void wrongTypeMethod(@LoginUser String str) {}
    }
}
