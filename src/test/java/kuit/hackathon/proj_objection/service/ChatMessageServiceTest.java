package kuit.hackathon.proj_objection.service;

import kuit.hackathon.proj_objection.dto.common.*;
import kuit.hackathon.proj_objection.entity.*;
import kuit.hackathon.proj_objection.exception.*;
import kuit.hackathon.proj_objection.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;
    @Mock
    private DebateAnalysisService debateAnalysisService;

    @InjectMocks
    private ChatMessageService chatMessageService;

    @BeforeEach
    void initTxSync() {
        // ✅ 단위 테스트에서 TransactionSynchronizationManager 쓰려면 동기화 활성화가 필요
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void clearTxSync() {
        // ✅ 다른 테스트에 영향 없게 반드시 정리
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("메시지 전송 성공")
    void sendMessage_success() {
        // given
        User sender = User.create("철수", "password123");
        ReflectionTestUtils.setField(sender, "id", 1L);

        ChatRoom chatRoom = ChatRoom.create(sender);
        Long chatRoomId = 1L;
        String content = "안녕하세요";

        ChatMessage savedMessage = ChatMessage.create(chatRoom, sender, content);
        ReflectionTestUtils.setField(savedMessage, "id", 1L);

        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, sender))
                .willReturn(Optional.of(ChatRoomMember.create(chatRoom, sender, ChatRoomMember.MemberRole.PARTICIPANT)));
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(savedMessage);

        // when
        ChatMessageDto result = chatMessageService.sendMessage(chatRoomId, sender, content);

        // then
        assertThat(result.getContent()).isEqualTo(content);
        assertThat(result.getSenderNickName()).isEqualTo("철수");
        then(chatMessageRepository).should(times(1)).save(any(ChatMessage.class));

        // ✅ 실제 트랜잭션 커밋은 단위 테스트에서 일어나지 않으니,
        // ✅ afterCommit을 직접 호출해서 커밋 후 동작을 시뮬레이션할 수 있음
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);

        // afterCommit에서 debateAnalysisService가 호출되는지 검증
        then(debateAnalysisService).should(times(1)).analyzeAndUpdateScores(chatRoomId);
    }

    @Test
    @DisplayName("OBSERVER는 메시지 전송 불가")
    void sendMessage_observer_fail() {
        // given
        User observer = User.create("민수", "password789");
        User creator = User.create("철수", "password123");
        ChatRoom chatRoom = ChatRoom.create(creator);
        Long chatRoomId = 1L;

        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, observer))
                .willReturn(Optional.of(ChatRoomMember.create(chatRoom, observer, ChatRoomMember.MemberRole.OBSERVER)));

        // when & then
        assertThatThrownBy(() -> chatMessageService.sendMessage(chatRoomId, observer, "메시지"))
                .isInstanceOf(MessageSendPermissionDeniedException.class);
    }

    @Test
    @DisplayName("메시지 목록 조회 성공 - ME/OTHER 구분")
    void getChatMessages_success() {
        // given
        User user = User.create("철수", "password123");
        User other = User.create("영희", "password456");

        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(other, "id", 2L);

        ChatRoom chatRoom = ChatRoom.create(user);
        Long chatRoomId = 1L;

        ChatMessage msg1 = ChatMessage.create(chatRoom, user, "내 메시지");
        ChatMessage msg2 = ChatMessage.create(chatRoom, other, "상대방 메시지");

        ReflectionTestUtils.setField(msg1, "id", 1L);
        ReflectionTestUtils.setField(msg2, "id", 2L);

        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, user))
                .willReturn(Optional.of(ChatRoomMember.create(chatRoom, user, ChatRoomMember.MemberRole.PARTICIPANT)));
        given(chatMessageRepository.findByChatRoomOrderByCreatedAtDesc(chatRoom))
                .willReturn(List.of(msg2, msg1));

        // when
        List<ChatMessageListDto> result = chatMessageService.getChatMessages(chatRoomId, user);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getType()).isEqualTo(ChatMessageDto.MessageType.OTHER);
        assertThat(result.get(1).getType()).isEqualTo(ChatMessageDto.MessageType.ME);
    }
}