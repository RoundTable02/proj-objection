package kuit.hackathon.proj_objection.controller;

import jakarta.servlet.http.HttpSession;
import kuit.hackathon.proj_objection.dto.BaseResponse;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.exception.UserNotFoundException;
import kuit.hackathon.proj_objection.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class TestControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpSession httpSession;

    @InjectMocks
    private TestController testController;

    @Test
    @DisplayName("test 엔드포인트 호출 시 성공 응답 반환")
    void test_shouldReturnSuccessResponse() {
        // given
        // 특별한 설정 없음

        // when
        BaseResponse<String> response = testController.test();

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult()).isEqualTo("test successful");
    }

    @Test
    @DisplayName("testSession 호출 시 로그인된 유저의 닉네임 반환")
    void testSession_withLoggedInUser_shouldReturnNickname() {
        // given
        Long userId = 1L;
        String nickname = "testUser";
        User user = User.create(nickname, "encodedPassword");

        given(httpSession.getAttribute("userId")).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        BaseResponse<String> response = testController.testSession(httpSession);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult()).isEqualTo(nickname);

        then(httpSession).should(times(1)).getAttribute("userId");
        then(userRepository).should(times(1)).findById(userId);
    }

    @Test
    @DisplayName("testSession 호출 시 세션에 userId 없으면 UserNotFoundException 발생")
    void testSession_withoutUserId_shouldThrowUserNotFoundException() {
        // given
        given(httpSession.getAttribute("userId")).willReturn(null);

        // when & then
        assertThatThrownBy(() -> testController.testSession(httpSession))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not logged in");

        then(userRepository).should(times(0)).findById(null);
    }

    @Test
    @DisplayName("testSession 호출 시 DB에 유저 없으면 UserNotFoundException 발생")
    void testSession_withInvalidUserId_shouldThrowUserNotFoundException() {
        // given
        Long invalidUserId = 999L;

        given(httpSession.getAttribute("userId")).willReturn(invalidUserId);
        given(userRepository.findById(invalidUserId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> testController.testSession(httpSession))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with ID: " + invalidUserId);

        then(httpSession).should(times(1)).getAttribute("userId");
        then(userRepository).should(times(1)).findById(invalidUserId);
    }
}
