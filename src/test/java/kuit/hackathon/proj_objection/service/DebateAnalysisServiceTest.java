package kuit.hackathon.proj_objection.service;

import kuit.hackathon.proj_objection.entity.ChatRoom;
import kuit.hackathon.proj_objection.entity.ChatRoomMember;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.repository.ChatRoomMemberRepository;
import kuit.hackathon.proj_objection.repository.ChatRoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class DebateAnalysisServiceTest {

    @Mock
    private OpenAiChatProcessor openAiChatProcessor;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @InjectMocks
    private DebateAnalysisService debateAnalysisService;

    @Test
    @DisplayName("AI 분석 후 ChatRoomMember의 percent를 정상적으로 업데이트")
    void analyzeAndUpdateScores_success() {
        // given
        Long chatRoomId = 1L;
        User creator = createUserWithId("철수", 1L);
        User participant = createUserWithId("영희", 2L);
        ChatRoom chatRoom = ChatRoom.create(creator);
        setChatRoomId(chatRoom, chatRoomId);

        ChatRoomMember creatorMember = ChatRoomMember.create(chatRoom, creator, ChatRoomMember.MemberRole.PARTICIPANT);
        ChatRoomMember participantMember = ChatRoomMember.create(chatRoom, participant, ChatRoomMember.MemberRole.PARTICIPANT);

        // AI 분석 결과 (철수: 65, 영희: 35)
        Map<String, Integer> scores = new HashMap<>();
        scores.put("철수", 65);
        scores.put("영희", 35);

        given(openAiChatProcessor.analyzePercent(chatRoomId)).willReturn(scores);
        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.findByChatRoom(chatRoom)).willReturn(List.of(creatorMember, participantMember));

        // when
        debateAnalysisService.analyzeAndUpdateScores(chatRoomId);

        // then
        then(openAiChatProcessor).should(times(1)).analyzePercent(chatRoomId);
        then(chatRoomRepository).should(times(1)).findById(chatRoomId);
        then(chatRoomMemberRepository).should(times(1)).findByChatRoom(chatRoom);

        // percent가 올바르게 업데이트되었는지 확인
        assertThat(creatorMember.getPercent()).isEqualTo(65);
        assertThat(participantMember.getPercent()).isEqualTo(35);
    }

    @Test
    @DisplayName("AI 분석 결과에 닉네임이 없으면 해당 멤버의 percent는 업데이트하지 않음")
    void analyzeAndUpdateScores_skipMemberNotInScores() {
        // given
        Long chatRoomId = 1L;
        User creator = createUserWithId("철수", 1L);
        User participant = createUserWithId("영희", 2L);
        User observer = createUserWithId("민수", 3L);
        ChatRoom chatRoom = ChatRoom.create(creator);
        setChatRoomId(chatRoom, chatRoomId);

        ChatRoomMember creatorMember = ChatRoomMember.create(chatRoom, creator, ChatRoomMember.MemberRole.PARTICIPANT);
        ChatRoomMember participantMember = ChatRoomMember.create(chatRoom, participant, ChatRoomMember.MemberRole.PARTICIPANT);
        ChatRoomMember observerMember = ChatRoomMember.create(chatRoom, observer, ChatRoomMember.MemberRole.OBSERVER);

        // AI 분석 결과에는 원고/피고만 포함 (관전자는 제외)
        Map<String, Integer> scores = new HashMap<>();
        scores.put("철수", 70);
        scores.put("영희", 30);

        given(openAiChatProcessor.analyzePercent(chatRoomId)).willReturn(scores);
        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.findByChatRoom(chatRoom))
                .willReturn(List.of(creatorMember, participantMember, observerMember));

        // when
        debateAnalysisService.analyzeAndUpdateScores(chatRoomId);

        // then
        assertThat(creatorMember.getPercent()).isEqualTo(70);
        assertThat(participantMember.getPercent()).isEqualTo(30);
        // 관전자는 점수가 없으므로 기본값 50 유지
        assertThat(observerMember.getPercent()).isEqualTo(50);
    }

    @Test
    @DisplayName("ChatRoom을 찾을 수 없으면 예외를 로그로 처리")
    void analyzeAndUpdateScores_chatRoomNotFound() {
        // given
        Long chatRoomId = 999L;

        Map<String, Integer> scores = new HashMap<>();
        scores.put("철수", 60);
        scores.put("영희", 40);

        given(openAiChatProcessor.analyzePercent(chatRoomId)).willReturn(scores);
        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.empty());

        // when - 예외가 발생해도 비동기 메서드는 예외를 던지지 않음
        debateAnalysisService.analyzeAndUpdateScores(chatRoomId);

        // then - 멤버 조회가 호출되지 않음
        then(chatRoomMemberRepository).should(never()).findByChatRoom(any());
    }

    @Test
    @DisplayName("OpenAI 분석 실패 시 예외를 로그로 처리하고 업데이트하지 않음")
    void analyzeAndUpdateScores_analysisError() {
        // given
        Long chatRoomId = 1L;

        given(openAiChatProcessor.analyzePercent(chatRoomId))
                .willThrow(new RuntimeException("OpenAI API 오류"));

        // when - 예외가 발생해도 비동기 메서드는 예외를 던지지 않음
        debateAnalysisService.analyzeAndUpdateScores(chatRoomId);

        // then - 나머지 작업이 실행되지 않음
        then(chatRoomRepository).should(never()).findById(any());
        then(chatRoomMemberRepository).should(never()).findByChatRoom(any());
    }

    @Test
    @DisplayName("빈 분석 결과를 받아도 정상 처리")
    void analyzeAndUpdateScores_emptyScores() {
        // given
        Long chatRoomId = 1L;
        User creator = createUserWithId("철수", 1L);
        User participant = createUserWithId("영희", 2L);
        ChatRoom chatRoom = ChatRoom.create(creator);
        setChatRoomId(chatRoom, chatRoomId);

        ChatRoomMember creatorMember = ChatRoomMember.create(chatRoom, creator, ChatRoomMember.MemberRole.PARTICIPANT);
        ChatRoomMember participantMember = ChatRoomMember.create(chatRoom, participant, ChatRoomMember.MemberRole.PARTICIPANT);

        // 빈 분석 결과
        Map<String, Integer> scores = new HashMap<>();

        given(openAiChatProcessor.analyzePercent(chatRoomId)).willReturn(scores);
        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.findByChatRoom(chatRoom)).willReturn(List.of(creatorMember, participantMember));

        // when
        debateAnalysisService.analyzeAndUpdateScores(chatRoomId);

        // then - 업데이트되지 않고 기본값 50 유지
        assertThat(creatorMember.getPercent()).isEqualTo(50);
        assertThat(participantMember.getPercent()).isEqualTo(50);
    }

    @Test
    @DisplayName("극단적인 점수 (0, 100)도 정상 처리")
    void analyzeAndUpdateScores_extremeScores() {
        // given
        Long chatRoomId = 1L;
        User creator = createUserWithId("철수", 1L);
        User participant = createUserWithId("영희", 2L);
        ChatRoom chatRoom = ChatRoom.create(creator);
        setChatRoomId(chatRoom, chatRoomId);

        ChatRoomMember creatorMember = ChatRoomMember.create(chatRoom, creator, ChatRoomMember.MemberRole.PARTICIPANT);
        ChatRoomMember participantMember = ChatRoomMember.create(chatRoom, participant, ChatRoomMember.MemberRole.PARTICIPANT);

        // 극단적인 점수
        Map<String, Integer> scores = new HashMap<>();
        scores.put("철수", 100);
        scores.put("영희", 0);

        given(openAiChatProcessor.analyzePercent(chatRoomId)).willReturn(scores);
        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.findByChatRoom(chatRoom)).willReturn(List.of(creatorMember, participantMember));

        // when
        debateAnalysisService.analyzeAndUpdateScores(chatRoomId);

        // then
        assertThat(creatorMember.getPercent()).isEqualTo(100);
        assertThat(participantMember.getPercent()).isEqualTo(0);
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
}
