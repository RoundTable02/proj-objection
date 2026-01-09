package kuit.hackathon.proj_objection.service;

import kuit.hackathon.proj_objection.dto.DebateStatusDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class DebateAnalysisService {

    private final OpenAiChatProcessor openAiChatProcessor;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 비동기로 토론 분석 후 결과를 브로드캐스트
     * 별도 스레드 풀에서 실행되어 메인 메시지 전송 흐름을 블로킹하지 않음
     *
     * @param chatRoomId 분석할 채팅방 ID
     */
    @Async("aiAnalysisExecutor")
    public void analyzeAndBroadcastAsync(Long chatRoomId) {
        try {
            log.debug("Starting AI analysis for chatRoomId: {}", chatRoomId);

            // OpenAI 분석 호출 (이 스레드에서 블로킹)
            Map<String, Integer> scores = openAiChatProcessor.analyzePercent(chatRoomId);

            // 결과 브로드캐스트
            DebateStatusDto statusDto = DebateStatusDto.of(scores);
            messagingTemplate.convertAndSend("/topic/chatroom/" + chatRoomId, statusDto);

            log.debug("AI analysis completed and broadcast for chatRoomId: {}", chatRoomId);

        } catch (Exception e) {
            // 비동기 에러는 로그만 남기고 메인 흐름에 영향 없음
            log.error("Failed to analyze debate for chatRoomId {}: {}", chatRoomId, e.getMessage(), e);
        }
    }
}
