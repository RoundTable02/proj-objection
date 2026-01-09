package kuit.hackathon.proj_objection.service;

import kuit.hackathon.proj_objection.dto.response.*;
import kuit.hackathon.proj_objection.entity.*;
import kuit.hackathon.proj_objection.exception.*;
import kuit.hackathon.proj_objection.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;
    @Mock
    private AsyncJudgmentService asyncJudgmentService;

    @InjectMocks
    private ChatRoomService chatRoomService;

    @Test
    @DisplayName("채팅방 생성 성공")
    void createChatRoom_success() {
        // given
        User creator = User.create("철수", "password123");
        ChatRoom chatRoom = ChatRoom.create(creator);

        given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(chatRoom);
        given(chatRoomMemberRepository.save(any(ChatRoomMember.class)))
                .willReturn(ChatRoomMember.create(chatRoom, creator, ChatRoomMember.MemberRole.PARTICIPANT));

        // when
        CreateChatRoomResponseDto result = chatRoomService.createChatRoom(creator);

        // then
        assertThat(result.getTitle()).isNotNull();
        assertThat(result.getParticipantCode()).matches("\\d{4}-\\d{4}");
        assertThat(result.getObserverCode()).matches("\\d{4}-\\d{4}");
        then(chatRoomRepository).should(times(1)).save(any(ChatRoom.class));
        then(chatRoomMemberRepository).should(times(1)).save(any(ChatRoomMember.class));
    }

    @Test
    @DisplayName("PARTICIPANT 코드로 입장 성공")
    void joinChatRoom_participant_success() {
        // given
        User creator = User.create("철수", "password123");
        User joiner = User.create("영희", "password456");
        ChatRoom chatRoom = ChatRoom.create(creator);
        String participantCode = chatRoom.getParticipantCode();

        given(chatRoomRepository.findByInviteCode(participantCode)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.countByChatRoomAndRoleAndUserNot(chatRoom, ChatRoomMember.MemberRole.PARTICIPANT, joiner)).willReturn(1);
        given(chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, joiner)).willReturn(Optional.empty());
        given(chatRoomMemberRepository.save(any(ChatRoomMember.class)))
                .willReturn(ChatRoomMember.create(chatRoom, joiner, ChatRoomMember.MemberRole.PARTICIPANT));

        // when
        JoinChatRoomResponseDto result = chatRoomService.joinChatRoom(participantCode, joiner);

        // then
        assertThat(result.getRole()).isEqualTo("PARTICIPANT");
        assertThat(result.getMessage()).contains("대화 상대방");
    }

    @Test
    @DisplayName("채팅방 인원 초과 시 입장 실패")
    void joinChatRoom_full_fail() {
        // given
        User creator = User.create("철수", "password123");
        User joiner = User.create("지훈", "password789");
        User other = User.create("다연", "password456");
        ChatRoom chatRoom = ChatRoom.create(creator);
        String participantCode = chatRoom.getParticipantCode();

        given(chatRoomRepository.findByInviteCode(participantCode)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.countByChatRoomAndRoleAndUserNot(chatRoom, ChatRoomMember.MemberRole.PARTICIPANT, joiner)).willReturn(1);
        given(chatRoomMemberRepository.countByChatRoomAndRoleAndUserNot(chatRoom,  ChatRoomMember.MemberRole.PARTICIPANT, other)).willReturn(2);
        given(chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, joiner)).willReturn(Optional.empty());
        given(chatRoomMemberRepository.save(any(ChatRoomMember.class)))
                .willReturn(ChatRoomMember.create(chatRoom, joiner, ChatRoomMember.MemberRole.PARTICIPANT));

        // when & then
        JoinChatRoomResponseDto result1 = chatRoomService.joinChatRoom(participantCode, joiner);
        JoinChatRoomResponseDto result2 = chatRoomService.joinChatRoom(participantCode, joiner);

        assertThat(result2.getRole()).isEqualTo("PARTICIPANT");
        assertThat(result2.getMessage()).contains("대화 상대방");
        assertThatThrownBy(() -> chatRoomService.joinChatRoom(participantCode, other))
                .isInstanceOf(ChatRoomFullException.class);
    }

    @Test
    @DisplayName("종료 요청 성공")
    void requestExit_success() {
        // given
        User creator = User.create("철수", "password123");
        ChatRoom chatRoom = ChatRoom.create(creator);
        Long chatRoomId = 1L;

        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, creator))
                .willReturn(Optional.of(ChatRoomMember.create(chatRoom, creator, ChatRoomMember.MemberRole.PARTICIPANT)));
        given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(chatRoom);

        // when
        ExitRequestResponseDto result = chatRoomService.requestExit(chatRoomId, creator);

        // then
        assertThat(result.getMessage()).contains("판결 요청");
        then(chatRoomRepository).should(times(1)).save(any(ChatRoom.class));
    }

    @Test
    @DisplayName("종료 수락 성공")
    void decideExit_approve_success() {
        // given
        User requester = User.create("철수", "password123");
        User decider = User.create("영희", "password456");

        // 실제 DB에 들어가지 않기 때문에 설정 필요
        ReflectionTestUtils.setField(requester, "id", 1L);
        ReflectionTestUtils.setField(decider, "id", 2L);

        ChatRoom chatRoom = ChatRoom.create(requester);
        chatRoom.requestExit(requester);
        Long chatRoomId = 1L;

        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, decider))
                .willReturn(Optional.of(ChatRoomMember.create(chatRoom, decider, ChatRoomMember.MemberRole.PARTICIPANT)));
        given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(chatRoom);

        // when
        ExitDecisionResponseDto result = chatRoomService.decideExit(chatRoomId, decider, true);

        // then
        assertThat(result.getApproved()).isTrue();
        assertThat(result.getMessage()).contains("확정");
        then(asyncJudgmentService).should(times(1)).analyzeAndSave(chatRoomId);
    }

    @Test
    @DisplayName("종료 거절 성공")
    void decideExit_reject_success() {
        // given
        User requester = User.create("철수", "password123");
        User decider = User.create("영희", "password456");

        // 실제 DB에 들어가지 않기 때문에 설정 필요
        ReflectionTestUtils.setField(requester, "id", 1L);
        ReflectionTestUtils.setField(decider, "id", 2L);

        ChatRoom chatRoom = ChatRoom.create(requester);
        chatRoom.requestExit(requester);
        Long chatRoomId = 1L;

        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, decider))
                .willReturn(Optional.of(ChatRoomMember.create(chatRoom, decider, ChatRoomMember.MemberRole.PARTICIPANT)));
        given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(chatRoom);

        // when
        ExitDecisionResponseDto result = chatRoomService.decideExit(chatRoomId, decider, false);

        // then
        assertThat(result.getApproved()).isFalse();
        assertThat(result.getMessage()).contains("거절");
        then(asyncJudgmentService).should(never()).analyzeAndSave(any());
    }
}