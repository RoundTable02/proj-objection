package kuit.hackathon.proj_objection.service;

import kuit.hackathon.proj_objection.dto.response.ChatPollResponseDto;
import kuit.hackathon.proj_objection.entity.ChatMessage;
import kuit.hackathon.proj_objection.entity.ChatRoom;
import kuit.hackathon.proj_objection.entity.ChatRoomMember;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.exception.ChatRoomMemberNotFoundException;
import kuit.hackathon.proj_objection.exception.ChatRoomNotFoundException;
import kuit.hackathon.proj_objection.repository.ChatMessageRepository;
import kuit.hackathon.proj_objection.repository.ChatRoomMemberRepository;
import kuit.hackathon.proj_objection.repository.ChatRoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ChatPollServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private ChatPollService chatPollService;

    @Test
    @DisplayName("폴링 성공 - lastMessageId 이후 메시지만 오름차순으로 반환")
    void poll_success_returnsMessagesAfterLastMessageId() {
        // given
        Long chatRoomId = 1L;
        Long lastMessageId = 10L;

        User creator = createUserWithId("철수", 1L);
        User participant = createUserWithId("영희", 2L);
        ChatRoom chatRoom = ChatRoom.create(creator);
        setChatRoomId(chatRoom, chatRoomId);

        ChatRoomMember creatorMember = ChatRoomMember.create(chatRoom, creator, ChatRoomMember.MemberRole.PARTICIPANT);
        ChatRoomMember participantMember = ChatRoomMember.create(chatRoom, participant, ChatRoomMember.MemberRole.PARTICIPANT);
        setMemberPercent(creatorMember, 60);
        setMemberPercent(participantMember, 40);

        ChatMessage msg1 = ChatMessage.create(chatRoom, creator, "메시지1");
        setMessageId(msg1, 11L);
        ChatMessage msg2 = ChatMessage.create(chatRoom, participant, "메시지2");
        setMessageId(msg2, 12L);

        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, creator)).willReturn(Optional.of(creatorMember));
        given(chatRoomMemberRepository.findByChatRoom(chatRoom)).willReturn(List.of(creatorMember, participantMember));
        given(chatMessageRepository.findByChatRoomAndIdGreaterThanOrderByIdAsc(chatRoom, lastMessageId))
                .willReturn(List.of(msg1, msg2));

        // when
        ChatPollResponseDto result = chatPollService.poll(chatRoomId, lastMessageId, creator);

        // then
        assertThat(result.getMessages()).hasSize(2);
        assertThat(result.getMessages().get(0).getMessageId()).isEqualTo(11L);
        assertThat(result.getMessages().get(0).getSenderNickname()).isEqualTo("철수");
        assertThat(result.getMessages().get(1).getMessageId()).isEqualTo(12L);
        assertThat(result.getMessages().get(1).getSenderNickname()).isEqualTo("영희");

        then(chatMessageRepository).should(times(1)).findByChatRoomAndIdGreaterThanOrderByIdAsc(chatRoom, lastMessageId);
    }

    @Test
    @DisplayName("폴링 성공 - lastMessageId가 null이면 0으로 처리하여 모든 메시지 반환")
    void poll_success_nullLastMessageIdTreatedAsZero() {
        // given
        Long chatRoomId = 1L;
        Long lastMessageId = null;

        User creator = createUserWithId("철수", 1L);
        ChatRoom chatRoom = ChatRoom.create(creator);
        setChatRoomId(chatRoom, chatRoomId);

        ChatRoomMember creatorMember = ChatRoomMember.create(chatRoom, creator, ChatRoomMember.MemberRole.PARTICIPANT);

        ChatMessage msg1 = ChatMessage.create(chatRoom, creator, "첫 메시지");
        setMessageId(msg1, 1L);

        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, creator)).willReturn(Optional.of(creatorMember));
        given(chatRoomMemberRepository.findByChatRoom(chatRoom)).willReturn(List.of(creatorMember));
        given(chatMessageRepository.findByChatRoomAndIdGreaterThanOrderByIdAsc(chatRoom, 0L))
                .willReturn(List.of(msg1));

        // when
        ChatPollResponseDto result = chatPollService.poll(chatRoomId, lastMessageId, creator);

        // then
        assertThat(result.getMessages()).hasSize(1);
        then(chatMessageRepository).should(times(1)).findByChatRoomAndIdGreaterThanOrderByIdAsc(chatRoom, 0L);
    }

    @Test
    @DisplayName("폴링 실패 - 채팅방이 존재하지 않으면 ChatRoomNotFoundException 발생")
    void poll_fail_chatRoomNotFound() {
        // given
        Long chatRoomId = 999L;
        User user = createUserWithId("철수", 1L);

        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chatPollService.poll(chatRoomId, 0L, user))
                .isInstanceOf(ChatRoomNotFoundException.class);

        then(chatRoomRepository).should(times(1)).findById(chatRoomId);
    }

    @Test
    @DisplayName("폴링 실패 - 채팅방 멤버가 아니면 ChatRoomMemberNotFoundException 발생")
    void poll_fail_notChatRoomMember() {
        // given
        Long chatRoomId = 1L;
        User creator = createUserWithId("철수", 1L);
        User nonMember = createUserWithId("비회원", 99L);
        ChatRoom chatRoom = ChatRoom.create(creator);
        setChatRoomId(chatRoom, chatRoomId);

        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, nonMember)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chatPollService.poll(chatRoomId, 0L, nonMember))
                .isInstanceOf(ChatRoomMemberNotFoundException.class);

        then(chatRoomMemberRepository).should(times(1)).findByChatRoomAndUser(chatRoom, nonMember);
    }

    @Test
    @DisplayName("폴링 성공 - exitRequester 존재 시 finishRequestNickname 세팅")
    void poll_success_finishRequestNicknameSetWhenExitRequesterExists() {
        // given
        Long chatRoomId = 1L;
        User creator = createUserWithId("철수", 1L);
        User participant = createUserWithId("영희", 2L);
        ChatRoom chatRoom = ChatRoom.create(creator);
        setChatRoomId(chatRoom, chatRoomId);
        chatRoom.requestExit(creator); // 철수가 종료 요청

        ChatRoomMember creatorMember = ChatRoomMember.create(chatRoom, creator, ChatRoomMember.MemberRole.PARTICIPANT);
        ChatRoomMember participantMember = ChatRoomMember.create(chatRoom, participant, ChatRoomMember.MemberRole.PARTICIPANT);

        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, participant)).willReturn(Optional.of(participantMember));
        given(chatRoomMemberRepository.findByChatRoom(chatRoom)).willReturn(List.of(creatorMember, participantMember));
        given(chatMessageRepository.findByChatRoomAndIdGreaterThanOrderByIdAsc(chatRoom, 0L)).willReturn(List.of());

        // when
        ChatPollResponseDto result = chatPollService.poll(chatRoomId, 0L, participant);

        // then
        assertThat(result.getFinishRequestNickname()).isEqualTo("철수");
        assertThat(result.getChatRoomStatus()).isEqualTo(ChatRoom.RoomStatus.REQUEST_FINISH);
    }

    @Test
    @DisplayName("폴링 성공 - exitRequester 없으면 finishRequestNickname은 null")
    void poll_success_finishRequestNicknameNullWhenNoExitRequester() {
        // given
        Long chatRoomId = 1L;
        User creator = createUserWithId("철수", 1L);
        ChatRoom chatRoom = ChatRoom.create(creator);
        setChatRoomId(chatRoom, chatRoomId);

        ChatRoomMember creatorMember = ChatRoomMember.create(chatRoom, creator, ChatRoomMember.MemberRole.PARTICIPANT);

        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, creator)).willReturn(Optional.of(creatorMember));
        given(chatRoomMemberRepository.findByChatRoom(chatRoom)).willReturn(List.of(creatorMember));
        given(chatMessageRepository.findByChatRoomAndIdGreaterThanOrderByIdAsc(chatRoom, 0L)).willReturn(List.of());

        // when
        ChatPollResponseDto result = chatPollService.poll(chatRoomId, 0L, creator);

        // then
        assertThat(result.getFinishRequestNickname()).isNull();
        assertThat(result.getChatRoomStatus()).isEqualTo(ChatRoom.RoomStatus.ALIVE);
    }

    @Test
    @DisplayName("폴링 성공 - percent는 PARTICIPANT만 포함하고 닉네임을 키로 사용")
    void poll_success_percentContainsOnlyParticipantsWithNicknameKey() {
        // given
        Long chatRoomId = 1L;
        User creator = createUserWithId("철수", 1L);
        User participant = createUserWithId("영희", 2L);
        User observer = createUserWithId("관전자", 3L);
        ChatRoom chatRoom = ChatRoom.create(creator);
        setChatRoomId(chatRoom, chatRoomId);

        ChatRoomMember creatorMember = ChatRoomMember.create(chatRoom, creator, ChatRoomMember.MemberRole.PARTICIPANT);
        ChatRoomMember participantMember = ChatRoomMember.create(chatRoom, participant, ChatRoomMember.MemberRole.PARTICIPANT);
        ChatRoomMember observerMember = ChatRoomMember.create(chatRoom, observer, ChatRoomMember.MemberRole.OBSERVER);
        setMemberPercent(creatorMember, 70);
        setMemberPercent(participantMember, 30);
        setMemberPercent(observerMember, 50); // OBSERVER는 무시되어야 함

        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, creator)).willReturn(Optional.of(creatorMember));
        given(chatRoomMemberRepository.findByChatRoom(chatRoom)).willReturn(List.of(creatorMember, participantMember, observerMember));
        given(chatMessageRepository.findByChatRoomAndIdGreaterThanOrderByIdAsc(chatRoom, 0L)).willReturn(List.of());

        // when
        ChatPollResponseDto result = chatPollService.poll(chatRoomId, 0L, creator);

        // then
        assertThat(result.getPercent()).hasSize(2);
        assertThat(result.getPercent()).containsEntry("철수", 70);
        assertThat(result.getPercent()).containsEntry("영희", 30);
        assertThat(result.getPercent()).doesNotContainKey("관전자");
    }

    @Test
    @DisplayName("폴링 성공 - PARTICIPANT가 1명뿐이어도 정상 동작")
    void poll_success_worksWithSingleParticipant() {
        // given
        Long chatRoomId = 1L;
        User creator = createUserWithId("철수", 1L);
        ChatRoom chatRoom = ChatRoom.create(creator);
        setChatRoomId(chatRoom, chatRoomId);

        ChatRoomMember creatorMember = ChatRoomMember.create(chatRoom, creator, ChatRoomMember.MemberRole.PARTICIPANT);
        setMemberPercent(creatorMember, 50);

        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, creator)).willReturn(Optional.of(creatorMember));
        given(chatRoomMemberRepository.findByChatRoom(chatRoom)).willReturn(List.of(creatorMember));
        given(chatMessageRepository.findByChatRoomAndIdGreaterThanOrderByIdAsc(chatRoom, 0L)).willReturn(List.of());

        // when
        ChatPollResponseDto result = chatPollService.poll(chatRoomId, 0L, creator);

        // then
        assertThat(result.getPercent()).hasSize(1);
        assertThat(result.getPercent()).containsEntry("철수", 50);
    }

    // ========== Helper Methods ==========

    private User createUserWithId(String nickname, Long id) {
        User user = User.create(nickname, "password");
        setUserId(user, id);
        return user;
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

    private void setChatRoomId(ChatRoom chatRoom, Long id) {
        try {
            var field = ChatRoom.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(chatRoom, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setMessageId(ChatMessage message, Long id) {
        try {
            var field = ChatMessage.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(message, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setMemberPercent(ChatRoomMember member, int percent) {
        try {
            var field = ChatRoomMember.class.getDeclaredField("percent");
            field.setAccessible(true);
            field.set(member, percent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
