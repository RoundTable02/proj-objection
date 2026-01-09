package kuit.hackathon.proj_objection.service;

import kuit.hackathon.proj_objection.dto.FinalJudgementResponseDto;
import kuit.hackathon.proj_objection.entity.ChatRoom;
import kuit.hackathon.proj_objection.entity.ChatRoomMember;
import kuit.hackathon.proj_objection.entity.FinalJudgement;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.exception.ChatRoomMemberNotFoundException;
import kuit.hackathon.proj_objection.exception.ChatRoomNotFoundException;
import kuit.hackathon.proj_objection.exception.FinalJudgementNotFoundException;
import kuit.hackathon.proj_objection.repository.ChatRoomMemberRepository;
import kuit.hackathon.proj_objection.repository.ChatRoomRepository;
import kuit.hackathon.proj_objection.repository.FinalJudgementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class FinalJudgementServiceTest {

    @Mock
    private FinalJudgementRepository finalJudgementRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @InjectMocks
    private FinalJudgementService finalJudgementService;

    @Test
    @DisplayName("최종 판결문 조회 성공")
    void getByChatRoomId_success() {
        // given
        Long chatRoomId = 1L;
        User creator = createUserWithId("철수", 1L);
        ChatRoom chatRoom = ChatRoom.create(creator);
        setChatRoomId(chatRoom, chatRoomId);

        ChatRoomMember creatorMember = ChatRoomMember.create(chatRoom, creator, ChatRoomMember.MemberRole.PARTICIPANT);

        FinalJudgement judgement = FinalJudgement.create(
                chatRoom,
                "철수",
                "철수",
                "영희",
                85,
                72,
                "원고가 논리적으로 주장을 펼쳤습니다.",
                "구체적 사례와 논리적 근거 제시",
                "감정적 대응으로 일관"
        );
        setJudgementId(judgement, 1L);

        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, creator)).willReturn(Optional.of(creatorMember));
        given(finalJudgementRepository.findByChatRoom_Id(chatRoomId)).willReturn(Optional.of(judgement));

        // when
        FinalJudgementResponseDto result = finalJudgementService.getByChatRoomId(chatRoomId, creator);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getChatRoomId()).isEqualTo(chatRoomId);
        assertThat(result.getWinner()).isEqualTo("철수");
        assertThat(result.getPlaintiff()).isEqualTo("철수");
        assertThat(result.getDefendant()).isEqualTo("영희");
        assertThat(result.getWinnerLogicScore()).isEqualTo(85);
        assertThat(result.getWinnerEmpathyScore()).isEqualTo(72);
        assertThat(result.getJudgmentComment()).isEqualTo("원고가 논리적으로 주장을 펼쳤습니다.");
        assertThat(result.getWinnerReason()).isEqualTo("구체적 사례와 논리적 근거 제시");
        assertThat(result.getLoserReason()).isEqualTo("감정적 대응으로 일관");

        then(chatRoomRepository).should(times(1)).findById(chatRoomId);
        then(chatRoomMemberRepository).should(times(1)).findByChatRoomAndUser(chatRoom, creator);
        then(finalJudgementRepository).should(times(1)).findByChatRoom_Id(chatRoomId);
    }

    @Test
    @DisplayName("최종 판결문 조회 실패 - 채팅방이 존재하지 않음")
    void getByChatRoomId_fail_chatRoomNotFound() {
        // given
        Long chatRoomId = 999L;
        User user = createUserWithId("철수", 1L);

        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> finalJudgementService.getByChatRoomId(chatRoomId, user))
                .isInstanceOf(ChatRoomNotFoundException.class);

        then(chatRoomRepository).should(times(1)).findById(chatRoomId);
    }

    @Test
    @DisplayName("최종 판결문 조회 실패 - 채팅방 멤버가 아님")
    void getByChatRoomId_fail_notChatRoomMember() {
        // given
        Long chatRoomId = 1L;
        User creator = createUserWithId("철수", 1L);
        User nonMember = createUserWithId("비회원", 99L);
        ChatRoom chatRoom = ChatRoom.create(creator);
        setChatRoomId(chatRoom, chatRoomId);

        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, nonMember)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> finalJudgementService.getByChatRoomId(chatRoomId, nonMember))
                .isInstanceOf(ChatRoomMemberNotFoundException.class);

        then(chatRoomMemberRepository).should(times(1)).findByChatRoomAndUser(chatRoom, nonMember);
    }

    @Test
    @DisplayName("최종 판결문 조회 실패 - 판결문이 존재하지 않음")
    void getByChatRoomId_fail_judgementNotFound() {
        // given
        Long chatRoomId = 1L;
        User creator = createUserWithId("철수", 1L);
        ChatRoom chatRoom = ChatRoom.create(creator);
        setChatRoomId(chatRoom, chatRoomId);

        ChatRoomMember creatorMember = ChatRoomMember.create(chatRoom, creator, ChatRoomMember.MemberRole.PARTICIPANT);

        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, creator)).willReturn(Optional.of(creatorMember));
        given(finalJudgementRepository.findByChatRoom_Id(chatRoomId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> finalJudgementService.getByChatRoomId(chatRoomId, creator))
                .isInstanceOf(FinalJudgementNotFoundException.class);

        then(finalJudgementRepository).should(times(1)).findByChatRoom_Id(chatRoomId);
    }

    @Test
    @DisplayName("최종 판결문 조회 성공 - OBSERVER도 조회 가능")
    void getByChatRoomId_success_observerCanView() {
        // given
        Long chatRoomId = 1L;
        User creator = createUserWithId("철수", 1L);
        User observer = createUserWithId("관전자", 3L);
        ChatRoom chatRoom = ChatRoom.create(creator);
        setChatRoomId(chatRoom, chatRoomId);

        ChatRoomMember observerMember = ChatRoomMember.create(chatRoom, observer, ChatRoomMember.MemberRole.OBSERVER);

        FinalJudgement judgement = FinalJudgement.create(
                chatRoom,
                "철수",
                "철수",
                "영희",
                85,
                72,
                "판결 코멘트",
                "승자 이유",
                "패자 이유"
        );
        setJudgementId(judgement, 1L);

        given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, observer)).willReturn(Optional.of(observerMember));
        given(finalJudgementRepository.findByChatRoom_Id(chatRoomId)).willReturn(Optional.of(judgement));

        // when
        FinalJudgementResponseDto result = finalJudgementService.getByChatRoomId(chatRoomId, observer);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getWinner()).isEqualTo("철수");
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

    private void setJudgementId(FinalJudgement judgement, Long id) {
        try {
            var field = FinalJudgement.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(judgement, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
