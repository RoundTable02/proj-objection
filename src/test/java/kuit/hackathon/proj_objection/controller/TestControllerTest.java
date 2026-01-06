package kuit.hackathon.proj_objection.controller;

import kuit.hackathon.proj_objection.dto.BaseResponse;
import kuit.hackathon.proj_objection.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestControllerTest {

    private final TestController testController = new TestController();

    @Test
    @DisplayName("test 엔드포인트 호출 시 성공 응답 반환")
    void test_shouldReturnSuccessResponse() {
        // when
        BaseResponse<String> response = testController.test();

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult()).isEqualTo("test successful");
    }

    @Test
    @DisplayName("testSession 호출 시 유저의 닉네임 반환")
    void testSession_withUser_shouldReturnNickname() {
        // given
        String nickname = "testUser";
        User user = User.create(nickname, "encodedPassword");

        // when
        BaseResponse<String> response = testController.testSession(user);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult()).isEqualTo(nickname);
    }
}
