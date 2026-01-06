package kuit.hackathon.proj_objection.service;

import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.exception.LoginException;
import kuit.hackathon.proj_objection.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginService loginService;

    @Test
    @DisplayName("신규 유저 로그인 시 회원가입 후 userId 반환")
    void login_newUser_shouldCreateAndReturnUserId() {
        // given
        String nickname = "testUser";
        String password = "password123";
        String encodedPassword = "encodedPassword123";
        Long expectedUserId = 1L;

        User savedUser = User.create(nickname, encodedPassword);
        setUserId(savedUser, expectedUserId);

        given(userRepository.findByNickname(nickname)).willReturn(Optional.empty());
        given(passwordEncoder.encode(password)).willReturn(encodedPassword);
        given(passwordEncoder.matches(password, encodedPassword)).willReturn(true);
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // when
        Long result = loginService.login(nickname, password);

        // then
        assertThat(result).isEqualTo(expectedUserId);
        then(userRepository).should(times(1)).findByNickname(nickname);
        then(passwordEncoder).should(times(1)).encode(password);
        then(userRepository).should(times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("기존 유저 로그인 시 비밀번호 일치하면 userId 반환")
    void login_existingUser_withCorrectPassword_shouldReturnUserId() {
        // given
        String nickname = "existingUser";
        String password = "correctPassword";
        String encodedPassword = "encodedCorrectPassword";
        Long expectedUserId = 2L;

        User existingUser = User.create(nickname, encodedPassword);
        setUserId(existingUser, expectedUserId);

        given(userRepository.findByNickname(nickname)).willReturn(Optional.of(existingUser));
        given(passwordEncoder.encode(password)).willReturn("unusedEncodedPassword"); // orElse는 eager evaluation
        given(passwordEncoder.matches(password, encodedPassword)).willReturn(true);
        given(userRepository.save(existingUser)).willReturn(existingUser);

        // when
        Long result = loginService.login(nickname, password);

        // then
        assertThat(result).isEqualTo(expectedUserId);
        then(userRepository).should(times(1)).findByNickname(nickname);
        then(userRepository).should(times(1)).save(existingUser);
    }

    @Test
    @DisplayName("기존 유저 로그인 시 비밀번호 불일치하면 LoginException 발생")
    void login_existingUser_withWrongPassword_shouldThrowLoginException() {
        // given
        String nickname = "existingUser";
        String wrongPassword = "wrongPassword";
        String encodedPassword = "encodedCorrectPassword";

        User existingUser = User.create(nickname, encodedPassword);

        given(userRepository.findByNickname(nickname)).willReturn(Optional.of(existingUser));
        given(passwordEncoder.encode(wrongPassword)).willReturn("unusedEncodedPassword"); // orElse는 eager evaluation
        given(passwordEncoder.matches(wrongPassword, encodedPassword)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> loginService.login(nickname, wrongPassword))
                .isInstanceOf(LoginException.class);

        then(userRepository).should(times(1)).findByNickname(nickname);
        then(userRepository).should(times(0)).save(any(User.class));
    }

    private void setUserId(User user, Long id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
