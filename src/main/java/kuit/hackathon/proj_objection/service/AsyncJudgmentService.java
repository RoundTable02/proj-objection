package kuit.hackathon.proj_objection.service;

import kuit.hackathon.proj_objection.dto.AnalysisResult;
import kuit.hackathon.proj_objection.dto.JudgmentNotificationDto;
import kuit.hackathon.proj_objection.entity.ChatRoom;
import kuit.hackathon.proj_objection.entity.ChatRoomMember;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.exception.ChatRoomNotFoundException;
import kuit.hackathon.proj_objection.exception.InsufficientParticipantsException;
import kuit.hackathon.proj_objection.repository.ChatRoomMemberRepository;
import kuit.hackathon.proj_objection.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AsyncJudgmentService {

    private final OpenAiChatProcessor openAiChatProcessor;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 비동기로 AI 판결 분석 후 결과를 브로드캐스트
     * 별도 스레드 풀에서 실행되어 메인 흐름을 블로킹하지 않음
     *
     * @param chatRoomId 분석할 채팅방 ID
     */
    @Async("aiAnalysisExecutor")
    public void analyzeAndBroadcast(Long chatRoomId) {
        try {
            log.debug("Starting judgment analysis for chatRoomId: {}", chatRoomId);

            // AI 정밀 분석 호출
            AnalysisResult result = openAiChatProcessor.analyzeDetailed(chatRoomId);

            // 참여자 정보 추출 (원고/피고)
            ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                    .orElseThrow(ChatRoomNotFoundException::new);

            ParticipantInfo participantInfo = extractParticipants(chatRoom);

            // 성공 알림 생성
            JudgmentNotificationDto notification = buildSuccessNotification(
                    result,
                    participantInfo.plaintiff,
                    participantInfo.defendant
            );

            // WebSocket 브로드캐스트
            messagingTemplate.convertAndSend("/topic/chatroom/" + chatRoomId + "/exit", notification);

            log.info("Judgment analysis completed and broadcast for chatRoomId: {}", chatRoomId);

        } catch (Exception e) {
            // 비동기 에러는 로그만 남기고 에러 알림 브로드캐스트
            log.error("Failed to analyze judgment for chatRoomId {}: {}", chatRoomId, e.getMessage(), e);

            // 에러 알림 생성 및 브로드캐스트
            JudgmentNotificationDto errorNotification = JudgmentNotificationDto.builder()
                    .type("JUDGMENT_ERROR")
                    .errorMessage("AI 분석 중 오류가 발생했습니다.")
                    .build();

            messagingTemplate.convertAndSend("/topic/chatroom/" + chatRoomId + "/exit", errorNotification);
        }
    }

    /**
     * 참여자 정보 추출 (원고 = 생성자, 피고 = 상대방)
     */
    private ParticipantInfo extractParticipants(ChatRoom chatRoom) {
        List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoom(chatRoom);

        // PARTICIPANT 역할만 필터링
        List<ChatRoomMember> participants = members.stream()
                .filter(m -> m.getRole() == ChatRoomMember.MemberRole.PARTICIPANT)
                .toList();

        if (participants.size() < 2) {
            throw new InsufficientParticipantsException();
        }

        // 원고 = 채팅방 생성자, 피고 = 다른 참여자
        User creator = chatRoom.getCreator();
        String plaintiff = null;
        String defendant = null;

        for (ChatRoomMember member : participants) {
            if (member.getUser().getId().equals(creator.getId())) {
                plaintiff = member.getUser().getNickname();
            } else if (defendant == null) {
                defendant = member.getUser().getNickname();
            }
        }

        if (plaintiff == null || defendant == null) {
            throw new InsufficientParticipantsException();
        }

        return new ParticipantInfo(plaintiff, defendant);
    }

    /**
     * 성공 알림 DTO 생성
     */
    private JudgmentNotificationDto buildSuccessNotification(
            AnalysisResult result,
            String plaintiff,
            String defendant) {

        return JudgmentNotificationDto.builder()
                .type("FINAL_JUDGMENT")
                .winner(result.getWinner())
                .plaintiff(plaintiff)
                .defendant(defendant)
                .winnerLogicScore(result.getWinnerLogicScore())
                .winnerEmpathyScore(result.getWinnerEmpathyScore())
                .judgmentComment(result.getJudgmentComment())
                .winnerReason(result.getWinnerReason())
                .loserReason(result.getLoserReason())
                .build();
    }

    /**
     * 참여자 정보 레코드
     */
    private record ParticipantInfo(String plaintiff, String defendant) {
    }
}
