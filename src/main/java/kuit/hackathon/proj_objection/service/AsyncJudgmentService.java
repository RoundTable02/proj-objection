package kuit.hackathon.proj_objection.service;

import jakarta.transaction.Transactional;
import kuit.hackathon.proj_objection.dto.common.AnalysisResult;
import kuit.hackathon.proj_objection.entity.ChatRoom;
import kuit.hackathon.proj_objection.entity.ChatRoomMember;
import kuit.hackathon.proj_objection.entity.FinalJudgement;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.exception.ChatRoomNotFoundException;
import kuit.hackathon.proj_objection.exception.InsufficientParticipantsException;
import kuit.hackathon.proj_objection.repository.ChatRoomMemberRepository;
import kuit.hackathon.proj_objection.repository.ChatRoomRepository;
import kuit.hackathon.proj_objection.repository.FinalJudgementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Transactional
@Slf4j
@RequiredArgsConstructor
@Service
public class AsyncJudgmentService {

    private final OpenAiChatProcessor openAiChatProcessor;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final FinalJudgementRepository finalJudgementRepository;
    private final ChatRoomCacheService chatRoomCacheService;

    /**
     * 비동기로 AI 판결 분석 후 결과를 DB에 저장
     * 별도 스레드 풀에서 실행되어 메인 흐름을 블로킹하지 않음
     *
     * @param chatRoomId 분석할 채팅방 ID
     */
    @Async("aiAnalysisExecutor")
    public void analyzeAndSave(Long chatRoomId) {
        try {
            log.debug("Starting judgment analysis for chatRoomId: {}", chatRoomId);

            // 이미 판결문이 존재하면 처리하지 않음
            if (finalJudgementRepository.existsByChatRoom_Id(chatRoomId)) {
                log.info("FinalJudgement already exists for chatRoomId: {}, skipping analysis", chatRoomId);
                return;
            }

            // 참여자 정보 추출 (원고/피고)
            ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                    .orElseThrow(ChatRoomNotFoundException::new);

            ParticipantInfo participantInfo = extractParticipants(chatRoom);

            // AI 정밀 분석 호출
            AnalysisResult result = openAiChatProcessor.analyzeDetailed(chatRoomId);

            // FinalJudgement 엔티티 생성 및 저장
            FinalJudgement finalJudgement = FinalJudgement.create(
                    chatRoom,
                    result.getWinner(),
                    participantInfo.plaintiff(),
                    participantInfo.defendant(),
                    result.getWinnerLogicScore(),
                    result.getWinnerEmpathyScore(),
                    result.getJudgmentComment(),
                    result.getWinnerReason(),
                    result.getLoserReason()
            );

            chatRoom.completeReport();
            chatRoomRepository.save(chatRoom);
            finalJudgementRepository.save(finalJudgement);

            // Redis status 캐시 업데이트 (DONE 상태로 변경)
            try {
                chatRoomCacheService.setStatus(chatRoomId, ChatRoom.RoomStatus.DONE, null);
                log.debug("Updated status cache to DONE for room {}", chatRoomId);
            } catch (Exception e) {
                log.warn("Failed to update status cache for room {}: {}", chatRoomId, e.getMessage());
            }

            log.info("Judgment analysis completed and saved for chatRoomId: {}", chatRoomId);

        } catch (Exception e) {
            // 비동기 에러는 로그만 남김
            log.error("Failed to analyze judgment for chatRoomId {}: {}", chatRoomId, e.getMessage(), e);
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
     * 참여자 정보 레코드
     */
    private record ParticipantInfo(String plaintiff, String defendant) {
    }
}
