package kuit.hackathon.proj_objection.controller;

import kuit.hackathon.proj_objection.dto.response.BaseResponse;
import kuit.hackathon.proj_objection.dto.common.ChatPollMessageDto;
import kuit.hackathon.proj_objection.dto.response.ChatPollResponseDto;
import kuit.hackathon.proj_objection.entity.ChatRoom;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.service.ChatPollService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ChatPollControllerTest {

    @Mock
    private ChatPollService chatPollService;

    @InjectMocks
    private ChatPollController chatPollController;

    @Test
    @DisplayName("폴링 API 호출 성공 - BaseResponse로 래핑하여 응답")
    void poll_success_returnsBaseResponseWithPollData() {
        // given
        Long chatRoomId = 1L;
        Long lastMessageId = 10L;
        User user = User.create("철수", "password");

        ChatPollMessageDto messageDto = new ChatPollMessageDto(
                11L, "철수", "안녕하세요!", LocalDateTime.of(2026, 1, 9, 12, 0, 0)
        );

        ChatPollResponseDto pollResponse = ChatPollResponseDto.builder()
                .messages(List.of(messageDto))
                .chatRoomStatus(ChatRoom.RoomStatus.ALIVE)
                .finishRequestNickname(null)
                .percent(Map.of("철수", 60, "영희", 40))
                .build();

        given(chatPollService.poll(chatRoomId, lastMessageId, user)).willReturn(pollResponse);

        // when
        BaseResponse<ChatPollResponseDto> response = chatPollController.poll(chatRoomId, lastMessageId, user);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getResult()).isEqualTo(pollResponse);
        assertThat(response.getResult().getMessages()).hasSize(1);
        assertThat(response.getResult().getChatRoomStatus()).isEqualTo(ChatRoom.RoomStatus.ALIVE);
        assertThat(response.getResult().getPercent()).containsEntry("철수", 60);

        then(chatPollService).should(times(1)).poll(chatRoomId, lastMessageId, user);
    }

    @Test
    @DisplayName("폴링 API 호출 시 lastMessageId null 허용")
    void poll_success_withNullLastMessageId() {
        // given
        Long chatRoomId = 1L;
        Long lastMessageId = null;
        User user = User.create("철수", "password");

        ChatPollResponseDto pollResponse = ChatPollResponseDto.builder()
                .messages(List.of())
                .chatRoomStatus(ChatRoom.RoomStatus.ALIVE)
                .finishRequestNickname(null)
                .percent(Map.of("철수", 50))
                .build();

        given(chatPollService.poll(chatRoomId, lastMessageId, user)).willReturn(pollResponse);

        // when
        BaseResponse<ChatPollResponseDto> response = chatPollController.poll(chatRoomId, lastMessageId, user);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult()).isEqualTo(pollResponse);

        then(chatPollService).should(times(1)).poll(chatRoomId, lastMessageId, user);
    }

    @Test
    @DisplayName("폴링 API 호출 시 finishRequestNickname 포함")
    void poll_success_withFinishRequestNickname() {
        // given
        Long chatRoomId = 1L;
        Long lastMessageId = 0L;
        User user = User.create("영희", "password");

        ChatPollResponseDto pollResponse = ChatPollResponseDto.builder()
                .messages(List.of())
                .chatRoomStatus(ChatRoom.RoomStatus.REQUEST_FINISH)
                .finishRequestNickname("철수")
                .percent(Map.of("철수", 70, "영희", 30))
                .build();

        given(chatPollService.poll(chatRoomId, lastMessageId, user)).willReturn(pollResponse);

        // when
        BaseResponse<ChatPollResponseDto> response = chatPollController.poll(chatRoomId, lastMessageId, user);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResult().getChatRoomStatus()).isEqualTo(ChatRoom.RoomStatus.REQUEST_FINISH);
        assertThat(response.getResult().getFinishRequestNickname()).isEqualTo("철수");

        then(chatPollService).should(times(1)).poll(chatRoomId, lastMessageId, user);
    }
}
