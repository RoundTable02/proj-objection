package kuit.hackathon.proj_objection.service;

import kuit.hackathon.proj_objection.entity.ChatRoom;
import kuit.hackathon.proj_objection.entity.ChatRoomMember;
import kuit.hackathon.proj_objection.exception.ChatRoomNotFoundException;
import kuit.hackathon.proj_objection.repository.ChatRoomMemberRepository;
import kuit.hackathon.proj_objection.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class DebateAnalysisService {

    private final OpenAiChatProcessor openAiChatProcessor;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    /**
     * 비동기로 토론 분석 후 ChatRoomMember의 percent 업데이트
     * 별도 스레드 풀에서 실행되어 메인 메시지 전송 흐름을 블로킹하지 않음
     *
     * @param chatRoomId 분석할 채팅방 ID
     */
    @Async("aiAnalysisExecutor")
    @Transactional
    public void analyzeAndUpdateScores(Long chatRoomId) {
        try {
            log.debug("Starting AI analysis for chatRoomId: {}", chatRoomId);

            // OpenAI 분석 호출 (이 스레드에서 블로킹)
            Map<String, Integer> scores = openAiChatProcessor.analyzePercent(chatRoomId);

            // ChatRoom 조회
            ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                    .orElseThrow(ChatRoomNotFoundException::new);

            // ChatRoomMember들의 percent 업데이트
            List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoom(chatRoom);
            for (ChatRoomMember member : members) {
                String nickname = member.getUser().getNickname();
                Integer score = scores.get(nickname);
                if (score != null) {
                    member.updatePercent(score);
                }
            }

            log.debug("AI analysis completed and percent updated for chatRoomId: {}", chatRoomId);

        } catch (Exception e) {
            // 비동기 에러는 로그만 남기고 메인 흐름에 영향 없음
            log.error("Failed to analyze debate for chatRoomId {}: {}", chatRoomId, e.getMessage(), e);
        }
    }
}
