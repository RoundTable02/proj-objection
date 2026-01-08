package kuit.hackathon.proj_objection.service;

import kuit.hackathon.proj_objection.dto.CreateChatRoomResponseDto;
import kuit.hackathon.proj_objection.dto.JoinChatRoomResponseDto;
import kuit.hackathon.proj_objection.entity.ChatRoom;
import kuit.hackathon.proj_objection.entity.ChatRoomMember;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.exception.AlreadyJoinedChatRoomException;
import kuit.hackathon.proj_objection.exception.ChatRoomNotFoundException;
import kuit.hackathon.proj_objection.exception.InvalidInviteCodeException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @InjectMocks
    private ChatRoomService chatRoomService;

    @Test
    @DisplayName("채팅방 생성 성공")
    void createChatRoom_success() {
        // given
        User creator = User.create("철수", "password123");
        String reward = "상품권 1만원";
        ChatRoom chatRoom = ChatRoom.create(creator, reward);

        given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(chatRoom);
        given(chatRoomMemberRepository.save(any(ChatRoomMember.class)))
                .willReturn(ChatRoomMember.create(chatRoom, creator, ChatRoomMember.MemberRole.PARTICIPANT));

        // when
        CreateChatRoomResponseDto result = chatRoomService.createChatRoom(creator, reward);

        // then
        assertThat(result.getTitle()).isNotNull();
        assertThat(result.getParticipantCode()).matches("\\d{4}-\\d{4}");
        assertThat(result.getObserverCode()).matches("\\d{4}-\\d{4}");
        assertThat(result.getParticipantCode()).isNotEqualTo(result.getObserverCode());

        then(chatRoomRepository).should(times(1)).save(any(ChatRoom.class));
        then(chatRoomMemberRepository).should(times(1)).save(any(ChatRoomMember.class));
    }

    @Test
    @DisplayName("PARTICIPANT 코드로 채팅방 입장 성공")
    void joinChatRoom_withParticipantCode_success() {
        // given
        User creator = User.create("철수", "password123");
        User joiner = User.create("영희", "password456");
        String reward = "상품권 1만원";
        ChatRoom chatRoom = ChatRoom.create(creator, reward);
        String participantCode = chatRoom.getParticipantCode();

        given(chatRoomRepository.findByInviteCode(participantCode)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.existsByChatRoomAndUser(chatRoom, joiner)).willReturn(false);
        given(chatRoomMemberRepository.save(any(ChatRoomMember.class)))
                .willReturn(ChatRoomMember.create(chatRoom, joiner, ChatRoomMember.MemberRole.PARTICIPANT));

        // when
        JoinChatRoomResponseDto result = chatRoomService.joinChatRoom(participantCode, joiner);

        // then
        assertThat(result.getRole()).isEqualTo("PARTICIPANT");
        assertThat(result.getMessage()).contains("대화 상대방");

        then(chatRoomRepository).should(times(1)).findByInviteCode(participantCode);
        then(chatRoomMemberRepository).should(times(1)).save(any(ChatRoomMember.class));
    }

    @Test
    @DisplayName("OBSERVER 코드로 채팅방 입장 성공")
    void joinChatRoom_withObserverCode_success() {
        // given
        User creator = User.create("철수", "password123");
        User observer = User.create("민수", "password789");
        String reward = "상품권 1만원";
        ChatRoom chatRoom = ChatRoom.create(creator, reward);
        String observerCode = chatRoom.getObserverCode();

        given(chatRoomRepository.findByInviteCode(observerCode)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.existsByChatRoomAndUser(chatRoom, observer)).willReturn(false);
        given(chatRoomMemberRepository.save(any(ChatRoomMember.class)))
                .willReturn(ChatRoomMember.create(chatRoom, observer, ChatRoomMember.MemberRole.OBSERVER));

        // when
        JoinChatRoomResponseDto result = chatRoomService.joinChatRoom(observerCode, observer);

        // then
        assertThat(result.getRole()).isEqualTo("OBSERVER");
        assertThat(result.getMessage()).contains("관전자");

        then(chatRoomRepository).should(times(1)).findByInviteCode(observerCode);
        then(chatRoomMemberRepository).should(times(1)).save(any(ChatRoomMember.class));
    }

    @Test
    @DisplayName("잘못된 초대 코드로 입장 실패")
    void joinChatRoom_withInvalidCode_throwsException() {
        // given
        User user = User.create("철수", "password123");
        String invalidCode = "9999-9999";

        given(chatRoomRepository.findByInviteCode(invalidCode)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chatRoomService.joinChatRoom(invalidCode, user))
                .isInstanceOf(InvalidInviteCodeException.class);

        then(chatRoomRepository).should(times(1)).findByInviteCode(invalidCode);
    }

    @Test
    @DisplayName("이미 입장한 채팅방에 재입장 시도 시 예외 발생")
    void joinChatRoom_alreadyJoined_throwsException() {
        // given
        User creator = User.create("철수", "password123");
        String reward = "상품권 1만원";
        ChatRoom chatRoom = ChatRoom.create(creator, reward);
        String participantCode = chatRoom.getParticipantCode();

        given(chatRoomRepository.findByInviteCode(participantCode)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.existsByChatRoomAndUser(chatRoom, creator)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> chatRoomService.joinChatRoom(participantCode, creator))
                .isInstanceOf(AlreadyJoinedChatRoomException.class);

        then(chatRoomMemberRepository).should(times(1)).existsByChatRoomAndUser(chatRoom, creator);
    }
}