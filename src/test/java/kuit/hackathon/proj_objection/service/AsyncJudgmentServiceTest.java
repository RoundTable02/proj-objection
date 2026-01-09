package kuit.hackathon.proj_objection.service;

import kuit.hackathon.proj_objection.dto.common.AnalysisResult;
import kuit.hackathon.proj_objection.entity.ChatRoom;
import kuit.hackathon.proj_objection.entity.ChatRoomMember;
import kuit.hackathon.proj_objection.entity.FinalJudgement;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.repository.ChatRoomMemberRepository;
import kuit.hackathon.proj_objection.repository.ChatRoomRepository;
import kuit.hackathon.proj_objection.repository.FinalJudgementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AsyncJudgmentServiceTest {

    @Mock
    private OpenAiChatProcessor openAiChatProcessor;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private FinalJudgementRepository finalJudgementRepository;

    @InjectMocks
    private AsyncJudgmentService asyncJudgmentService;

    @Test
    @DisplayName("AI 분석 및 판결문 저장 성공")
    void analyzeAndSave_success() {
        // given
        Long chatRoomId = 1L;
        User creator = createUserWithId("철수", 1L);
        User participant = createUserWithId("영희", 2L);
        ChatRoom chatRoom = ChatRoom.create(creator);
        setChatRoomId(chatRoom, chatRoomId);

        ChatRoomMember creatorMember = ChatRoomMember.create(chatRoom, creator, ChatRoomMember.MemberRole.PARTICIPANT);
        ChatRoomMember participantMember = ChatRoomMember.create(chatRoom, participant, ChatRoomMember.MemberRole.PARTICIPANT);

        AnalysisResult analysisResult = AnalysisResult.builder()
                .winner("철수")
                .winnerLogicScore(85)
                .winnerEmpathyScore(72)
                .judgmentComment("원고가 논리적으로 주장을 펼쳤습니다.")
                .winnerReason("구체적 사례와 논리적 근거 제시")
                .loserReason("감정적 대응으로 일관")
                .build();

        given(finalJudgementRepository.existsByChatRoom_Id(chatRoomId)).willReturn(false);
        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.findByChatRoom(chatRoom)).willReturn(List.of(creatorMember, participantMember));
        given(openAiChatProcessor.analyzeDetailed(chatRoomId)).willReturn(analysisResult);

        // when
        asyncJudgmentService.analyzeAndSave(chatRoomId);

        // then
        ArgumentCaptor<FinalJudgement> captor = ArgumentCaptor.forClass(FinalJudgement.class);
        then(finalJudgementRepository).should(times(1)).save(captor.capture());

        FinalJudgement savedJudgement = captor.getValue();
        assertThat(savedJudgement.getChatRoom()).isEqualTo(chatRoom);
        assertThat(savedJudgement.getWinner()).isEqualTo("철수");
        assertThat(savedJudgement.getPlaintiff()).isEqualTo("철수");
        assertThat(savedJudgement.getDefendant()).isEqualTo("영희");
        assertThat(savedJudgement.getWinnerLogicScore()).isEqualTo(85);
        assertThat(savedJudgement.getWinnerEmpathyScore()).isEqualTo(72);
        assertThat(savedJudgement.getJudgmentComment()).isEqualTo("원고가 논리적으로 주장을 펼쳤습니다.");
        assertThat(savedJudgement.getWinnerReason()).isEqualTo("구체적 사례와 논리적 근거 제시");
        assertThat(savedJudgement.getLoserReason()).isEqualTo("감정적 대응으로 일관");
    }

    @Test
    @DisplayName("이미 판결문이 존재하면 분석을 스킵")
    void analyzeAndSave_skip_whenJudgementAlreadyExists() {
        // given
        Long chatRoomId = 1L;

        given(finalJudgementRepository.existsByChatRoom_Id(chatRoomId)).willReturn(true);

        // when
        asyncJudgmentService.analyzeAndSave(chatRoomId);

        // then
        then(openAiChatProcessor).should(never()).analyzeDetailed(any());
        then(finalJudgementRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("AI 분석 실패 시 예외를 로그로 처리하고 저장하지 않음")
    void analyzeAndSave_fail_analysisError() {
        // given
        Long chatRoomId = 1L;
        User creator = createUserWithId("철수", 1L);
        User participant = createUserWithId("영희", 2L);
        ChatRoom chatRoom = ChatRoom.create(creator);
        setChatRoomId(chatRoom, chatRoomId);

        ChatRoomMember creatorMember = ChatRoomMember.create(chatRoom, creator, ChatRoomMember.MemberRole.PARTICIPANT);
        ChatRoomMember participantMember = ChatRoomMember.create(chatRoom, participant, ChatRoomMember.MemberRole.PARTICIPANT);

        given(finalJudgementRepository.existsByChatRoom_Id(chatRoomId)).willReturn(false);
        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.findByChatRoom(chatRoom)).willReturn(List.of(creatorMember, participantMember));
        given(openAiChatProcessor.analyzeDetailed(chatRoomId)).willThrow(new RuntimeException("OpenAI API 오류"));

        // when - 예외가 발생해도 비동기 메서드는 예외를 던지지 않음
        asyncJudgmentService.analyzeAndSave(chatRoomId);

        // then
        then(finalJudgementRepository).should(never()).save(any());
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
