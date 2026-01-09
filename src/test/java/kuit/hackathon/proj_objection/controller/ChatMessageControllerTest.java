package kuit.hackathon.proj_objection.controller;

import kuit.hackathon.proj_objection.dto.common.ChatMessageDto;
import kuit.hackathon.proj_objection.dto.common.ChatMessageListDto;
import kuit.hackathon.proj_objection.dto.request.SendMessageRequestDto;
import kuit.hackathon.proj_objection.dto.response.BaseResponse;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.exception.ChatRoomMemberNotFoundException;
import kuit.hackathon.proj_objection.exception.ChatRoomNotFoundException;
import kuit.hackathon.proj_objection.exception.MessageSendPermissionDeniedException;
import kuit.hackathon.proj_objection.service.ChatMessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ChatMessageControllerTest {

    @Mock
    private ChatMessageService chatMessageService;

    @InjectMocks
    private ChatMessageController chatMessageController;

    private static User userWithId(long id, String nickname) {
        User u = User.create(nickname, "pw");
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    @Test
    @DisplayName("메시지 전송 성공 - service 호출 결과를 BaseResponse로 감싸서 반환")
    void sendMessage_success() {
        // given
        Long chatRoomId = 1L;
        User loginUser = userWithId(10L, "철수");
        String content = "안녕하세요";
        SendMessageRequestDto request = new SendMessageRequestDto(content);

        ChatMessageDto serviceResult = new ChatMessageDto(
                100L,
                loginUser.getId(),
                loginUser.getNickname(),
                content,
                LocalDateTime.now(),
                ChatMessageDto.MessageType.OTHER
        );

        given(chatMessageService.sendMessage(chatRoomId, loginUser, content))
                .willReturn(serviceResult);

        // when
        BaseResponse<ChatMessageDto> response =
                chatMessageController.sendMessage(chatRoomId, request, null, loginUser);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult()).isNotNull();
        assertThat(response.getResult().getContent()).isEqualTo("안녕하세요");
        assertThat(response.getResult().getSenderNickName()).isEqualTo("철수");

        then(chatMessageService).should(times(1))
                .sendMessage(chatRoomId, loginUser, content);
    }

    @Test
    @DisplayName("메시지 전송 실패 - 채팅방이 없으면 ChatRoomNotFoundException 전파")
    void sendMessage_fail_chatRoomNotFound() {
        // given
        Long chatRoomId = 1L;
        User loginUser = userWithId(10L, "철수");
        SendMessageRequestDto request = new SendMessageRequestDto("안녕");

        given(chatMessageService.sendMessage(eq(chatRoomId), eq(loginUser), anyString()))
                .willThrow(new ChatRoomNotFoundException());

        // when & then
        assertThatThrownBy(() ->
                chatMessageController.sendMessage(chatRoomId, request, null, loginUser)
        ).isInstanceOf(ChatRoomNotFoundException.class);

        then(chatMessageService).should(times(1))
                .sendMessage(chatRoomId, loginUser, "안녕");
    }

    @Test
    @DisplayName("메시지 전송 실패 - OBSERVER 등 권한 없으면 MessageSendPermissionDeniedException 전파")
    void sendMessage_fail_permissionDenied() {
        // given
        Long chatRoomId = 1L;
        User loginUser = userWithId(10L, "관전자");
        SendMessageRequestDto request = new SendMessageRequestDto("못보냄");

        given(chatMessageService.sendMessage(chatRoomId, loginUser, "못보냄"))
                .willThrow(new MessageSendPermissionDeniedException());

        // when & then
        assertThatThrownBy(() ->
                chatMessageController.sendMessage(chatRoomId, request, null, loginUser)
        ).isInstanceOf(MessageSendPermissionDeniedException.class);

        then(chatMessageService).should(times(1))
                .sendMessage(chatRoomId, loginUser, "못보냄");
    }

    @Test
    @DisplayName("메시지 목록 조회 성공 - service 결과 리스트를 BaseResponse로 감싸서 반환")
    void getChatMessages_success() {
        // given
        Long chatRoomId = 1L;
        User loginUser = userWithId(10L, "철수");

        List<ChatMessageListDto> serviceResult = List.of(
                new ChatMessageListDto(1L, "철수", "첫 메시지", LocalDateTime.now(), ChatMessageDto.MessageType.ME),
                new ChatMessageListDto(2L, "영희", "상대 메시지", LocalDateTime.now(), ChatMessageDto.MessageType.OTHER)
        );

        given(chatMessageService.getChatMessages(chatRoomId, loginUser))
                .willReturn(serviceResult);

        // when
        BaseResponse<List<ChatMessageListDto>> response =
                chatMessageController.getChatMessages(chatRoomId, null, loginUser);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult()).hasSize(2);
        assertThat(response.getResult().get(0).getSenderNickname()).isEqualTo("철수");
        assertThat(response.getResult().get(1).getContent()).isEqualTo("상대 메시지");

        then(chatMessageService).should(times(1)).getChatMessages(chatRoomId, loginUser);
    }

    @Test
    @DisplayName("메시지 목록 조회 실패 - 채팅방 멤버가 아니면 ChatRoomMemberNotFoundException 전파")
    void getChatMessages_fail_notMember() {
        // given
        Long chatRoomId = 1L;
        User loginUser = userWithId(10L, "철수");

        given(chatMessageService.getChatMessages(chatRoomId, loginUser))
                .willThrow(new ChatRoomMemberNotFoundException());

        // when & then
        assertThatThrownBy(() ->
                chatMessageController.getChatMessages(chatRoomId, null, loginUser)
        ).isInstanceOf(ChatRoomMemberNotFoundException.class);

        then(chatMessageService).should(times(1)).getChatMessages(chatRoomId, loginUser);
    }
}
