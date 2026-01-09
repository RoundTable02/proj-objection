package kuit.hackathon.proj_objection.controller;

import kuit.hackathon.proj_objection.dto.response.BaseResponse;
import kuit.hackathon.proj_objection.dto.response.CreateChatRoomResponseDto;
import kuit.hackathon.proj_objection.dto.response.ExitDecisionResponseDto;
import kuit.hackathon.proj_objection.dto.response.ExitRequestResponseDto;
import kuit.hackathon.proj_objection.dto.response.JoinChatRoomResponseDto;
import kuit.hackathon.proj_objection.dto.request.ExitDecisionRequestDto;
import kuit.hackathon.proj_objection.dto.request.JoinChatRoomRequestDto;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.exception.ChatRoomFullException;
import kuit.hackathon.proj_objection.exception.ChatRoomMemberNotFoundException;
import kuit.hackathon.proj_objection.exception.ChatRoomNotFoundException;
import kuit.hackathon.proj_objection.exception.ExitDecisionPermissionDeniedException;
import kuit.hackathon.proj_objection.exception.InvalidInviteCodeException;
import kuit.hackathon.proj_objection.exception.NoExitRequestException;
import kuit.hackathon.proj_objection.service.ChatRoomService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ChatRoomControllerTest {

    @Mock
    private ChatRoomService chatRoomService;

    @InjectMocks
    private ChatRoomController chatRoomController;

    private static User userWithId(long id, String nickname) {
        User u = User.create(nickname, "pw");
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    // -------------------------
    // createChatRoom
    // -------------------------

    @Test
    @DisplayName("채팅방 생성 성공 - BaseResponse로 감싸서 반환")
    void createChatRoom_success() {
        // given
        User user = userWithId(1L, "철수");

        CreateChatRoomResponseDto serviceResult = new CreateChatRoomResponseDto(
                10L, "2026가단AI01080001", "1234-5678", "8765-4321"
        );

        given(chatRoomService.createChatRoom(user)).willReturn(serviceResult);

        // when
        BaseResponse<CreateChatRoomResponseDto> response = chatRoomController.createChatRoom(user);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult().getChatRoomId()).isEqualTo(10L);
        assertThat(response.getResult().getParticipantCode()).isEqualTo("1234-5678");

        then(chatRoomService).should(times(1)).createChatRoom(user);
    }

    // -------------------------
    // joinChatRoom
    // -------------------------

    @Test
    @DisplayName("채팅방 입장 성공 - inviteCode로 joinChatRoom 호출 후 BaseResponse 반환")
    void joinChatRoom_success() {
        // given
        User user = userWithId(2L, "영희");
        JoinChatRoomRequestDto req = new JoinChatRoomRequestDto("1234-5678");

        JoinChatRoomResponseDto serviceResult = new JoinChatRoomResponseDto(
                10L, "2026가단AI01080001", "PARTICIPANT", "대화 상대방으로 입장했습니다."
        );

        given(chatRoomService.joinChatRoom("1234-5678", user)).willReturn(serviceResult);

        // when
        BaseResponse<JoinChatRoomResponseDto> response = chatRoomController.joinChatRoom(req, user);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult().getChatRoomId()).isEqualTo(10L);
        assertThat(response.getResult().getRole()).isEqualTo("PARTICIPANT");

        then(chatRoomService).should(times(1)).joinChatRoom("1234-5678", user);
    }

    @Test
    @DisplayName("채팅방 입장 실패 - 초대 코드가 유효하지 않으면 예외 전파")
    void joinChatRoom_fail_invalidInviteCode() {
        // given
        User user = userWithId(2L, "영희");
        JoinChatRoomRequestDto req = new JoinChatRoomRequestDto("wrong-code");

        given(chatRoomService.joinChatRoom("wrong-code", user))
                .willThrow(new InvalidInviteCodeException());

        // when & then
        assertThatThrownBy(() -> chatRoomController.joinChatRoom(req, user))
                .isInstanceOf(InvalidInviteCodeException.class);

        then(chatRoomService).should(times(1)).joinChatRoom("wrong-code", user);
    }

    @Test
    @DisplayName("채팅방 입장 실패 - 방이 꽉 찼으면 ChatRoomFullException 전파")
    void joinChatRoom_fail_full() {
        // given
        User user = userWithId(2L, "영희");
        JoinChatRoomRequestDto req = new JoinChatRoomRequestDto("1234-5678");

        given(chatRoomService.joinChatRoom("1234-5678", user))
                .willThrow(new ChatRoomFullException());

        // when & then
        assertThatThrownBy(() -> chatRoomController.joinChatRoom(req, user))
                .isInstanceOf(ChatRoomFullException.class);

        then(chatRoomService).should(times(1)).joinChatRoom("1234-5678", user);
    }

    // -------------------------
    // requestExit
    // -------------------------

    @Test
    @DisplayName("채팅방 종료 요청 성공 - chatRoomId로 requestExit 호출 후 BaseResponse 반환")
    void requestExit_success() {
        // given
        User user = userWithId(1L, "철수");
        Long chatRoomId = 10L;

        ExitRequestResponseDto serviceResult = new ExitRequestResponseDto(
                chatRoomId, "철수", "판결 요청이 전송되었습니다."
        );

        given(chatRoomService.requestExit(chatRoomId, user)).willReturn(serviceResult);

        // when
        BaseResponse<ExitRequestResponseDto> response = chatRoomController.requestExit(chatRoomId, user);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult().getChatRoomId()).isEqualTo(chatRoomId);
        assertThat(response.getResult().getRequesterNickname()).isEqualTo("철수");

        then(chatRoomService).should(times(1)).requestExit(chatRoomId, user);
    }

    @Test
    @DisplayName("채팅방 종료 요청 실패 - 채팅방이 없으면 ChatRoomNotFoundException 전파")
    void requestExit_fail_roomNotFound() {
        // given
        User user = userWithId(1L, "철수");
        Long chatRoomId = 999L;

        given(chatRoomService.requestExit(chatRoomId, user))
                .willThrow(new ChatRoomNotFoundException());

        // when & then
        assertThatThrownBy(() -> chatRoomController.requestExit(chatRoomId, user))
                .isInstanceOf(ChatRoomNotFoundException.class);

        then(chatRoomService).should(times(1)).requestExit(chatRoomId, user);
    }

    // -------------------------
    // decideExit
    // -------------------------

    @Test
    @DisplayName("채팅방 종료 결정 성공(수락) - approve=true")
    void decideExit_approve_success() {
        // given
        User user = userWithId(2L, "영희");
        Long chatRoomId = 10L;

        ExitDecisionRequestDto req = new ExitDecisionRequestDto(true);

        ExitDecisionResponseDto serviceResult = new ExitDecisionResponseDto(
                chatRoomId, true, "판결이 확정되었습니다."
        );

        given(chatRoomService.decideExit(chatRoomId, user, true))
                .willReturn(serviceResult);

        // when
        BaseResponse<ExitDecisionResponseDto> response = chatRoomController.decideExit(chatRoomId, req, user);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult().getApproved()).isTrue();
        assertThat(response.getResult().getMessage()).contains("확정");

        then(chatRoomService).should(times(1)).decideExit(chatRoomId, user, true);
    }

    @Test
    @DisplayName("채팅방 종료 결정 실패 - 종료 요청이 없으면 NoExitRequestException 전파")
    void decideExit_fail_noExitRequest() {
        // given
        User user = userWithId(2L, "영희");
        Long chatRoomId = 10L;
        ExitDecisionRequestDto req = new ExitDecisionRequestDto(true);

        given(chatRoomService.decideExit(chatRoomId, user, true))
                .willThrow(new NoExitRequestException());

        // when & then
        assertThatThrownBy(() -> chatRoomController.decideExit(chatRoomId, req, user))
                .isInstanceOf(NoExitRequestException.class);

        then(chatRoomService).should(times(1)).decideExit(chatRoomId, user, true);
    }

    @Test
    @DisplayName("채팅방 종료 결정 실패 - 권한 없으면 ExitDecisionPermissionDeniedException 전파")
    void decideExit_fail_permissionDenied() {
        // given
        User user = userWithId(2L, "영희");
        Long chatRoomId = 10L;
        ExitDecisionRequestDto req = new ExitDecisionRequestDto(false);

        given(chatRoomService.decideExit(chatRoomId, user, false))
                .willThrow(new ExitDecisionPermissionDeniedException());

        // when & then
        assertThatThrownBy(() -> chatRoomController.decideExit(chatRoomId, req, user))
                .isInstanceOf(ExitDecisionPermissionDeniedException.class);

        then(chatRoomService).should(times(1)).decideExit(chatRoomId, user, false);
    }

    @Test
    @DisplayName("채팅방 종료 결정 실패 - 멤버 정보가 없으면 ChatRoomMemberNotFoundException 전파")
    void decideExit_fail_memberNotFound() {
        // given
        User user = userWithId(2L, "영희");
        Long chatRoomId = 10L;
        ExitDecisionRequestDto req = new ExitDecisionRequestDto(false);

        given(chatRoomService.decideExit(chatRoomId, user, false))
                .willThrow(new ChatRoomMemberNotFoundException());

        // when & then
        assertThatThrownBy(() -> chatRoomController.decideExit(chatRoomId, req, user))
                .isInstanceOf(ChatRoomMemberNotFoundException.class);

        then(chatRoomService).should(times(1)).decideExit(chatRoomId, user, false);
    }
}
