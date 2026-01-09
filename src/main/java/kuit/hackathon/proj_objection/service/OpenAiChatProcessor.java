package kuit.hackathon.proj_objection.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kuit.hackathon.proj_objection.client.openai.OpenAiClient;
import kuit.hackathon.proj_objection.client.openai.dto.ChatCompletionRequest;
import kuit.hackathon.proj_objection.client.openai.dto.ChatCompletionResponse;
import kuit.hackathon.proj_objection.client.openai.dto.Message;
import kuit.hackathon.proj_objection.dto.AnalysisResult;
import kuit.hackathon.proj_objection.entity.ChatMessage;
import kuit.hackathon.proj_objection.entity.ChatRoom;
import kuit.hackathon.proj_objection.entity.ChatRoomMember;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.exception.AnalysisParseException;
import kuit.hackathon.proj_objection.exception.ChatRoomNotFoundException;
import kuit.hackathon.proj_objection.exception.InsufficientParticipantsException;
import kuit.hackathon.proj_objection.repository.ChatMessageRepository;
import kuit.hackathon.proj_objection.repository.ChatRoomMemberRepository;
import kuit.hackathon.proj_objection.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class OpenAiChatProcessor {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    private static final String MODEL = "gpt-4o";

    private static final String PERCENT_SYSTEM_PROMPT = """
            당신은 원고와 피고 두 사용자의 논쟁을 심판하는 재판관입니다.

            두 사용자 중 원고가 더 설득력 있는 주장을 펼쳤다면, 100에 가까운 퍼센트 점수를 부여하고,
            피고가 더 설득력 있는 주장을 펼쳤다면, 0에 가까운 점수를 부여하세요.
            양측이 비슷한 주장을 펼쳤다면, 50에 가까운 점수를 부여하세요.
            점수는 항상 양의 정수로 응답해야 하며, 소수점 이하는 버리세요.

            <채점 기준>
            1. 논리적 근거 제시 시 가산점
            2. 감정적 비난, 욕설 시 감점
            3. 했던 말 반복 시 감점
            </채점 기준>

            <응답 형식>
            점수는 다음 형식의 JSON으로 응답하세요.
            {
                "score": 정수형_점수
            }
            """;

    private static final String DETAILED_SYSTEM_PROMPT = """
            당신은 원고와 피고 두 사용자의 논쟁을 심판하는 재판관입니다.
            두 사용자 중 누가 더 설득력 있는 주장을 펼쳤는지 판단하고, 한 명의 승자를 선택하세요.

            두 사용자 중 원고가 더 설득력 있는 주장을 펼쳤다면, 원고의 논리력과 공감력 점수를 각각 0에서 100 사이의 정수로 부여하세요.
            피고가 더 설득력 있는 주장을 펼쳤다면, 피고의 논리력과 공감력 점수를 각각 0에서 100 사이의 정수로 부여하세요.
            양측이 비슷한 주장을 펼쳤다면, 두 사용자 모두에게 비슷한 점수를 부여하세요.
            점수는 항상 양의 정수로 응답해야 하며, 소수점 이하는 버리세요.

            judgment_comment에서는 재판 전체의 내용에 대한 코멘트를 남기세요.
            winner_reason에서는 승자가 가산점을 받은 이유를 구체적으로 설명하세요.
            loser_reason에서는 패자가 감점된 이유를 구체적으로 설명하세요.

            <채점 기준>
            1. 논리적 근거 제시 시 가산점
            2. 감정적 비난, 욕설 시 감점
            3. 했던 말 반복 시 감점
            </채점 기준>

            응답은 반드시 다음 형식의 JSON으로 작성하세요.
            <응답 형식>
            {
                "winner": "원고" 또는 "피고",
                "winner_logic_score": 승자의 논리력 점수 (0~100 정수),
                "winner_empathy_score": 승자의 공감력 점수 (0~100 정수),
                "judgment_comment": "심판 코멘트 문자열",
                "winner_reason": "심판이 승자를 결정한 이유 문자열",
                "loser_reason": "심판이 패자를 결정한 이유 문자열"
            }
            """;

    /**
     * 퍼센트 분석: A의 점수를 0-100으로 분석하고, B는 100-A로 계산
     *
     * @param chatRoomId 채팅방 ID
     * @return 닉네임과 점수를 매핑한 Map
     */
    @Transactional(readOnly = true)
    public Map<String, Integer> analyzePercent(Long chatRoomId) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        ParticipantPair participants = getParticipants(chatRoom);
        List<ChatMessage> messages = getMessagesChronological(chatRoom);
        String formattedMessages = formatMessagesForAi(messages, participants);

//        String response = callOpenAi(PERCENT_SYSTEM_PROMPT, formattedMessages);
//        int scoreA = parsePercentResponse(response);

        // 0 - 100 사이의 임의의 점수 생성
        int scoreA = (int) (Math.random() * 101);
        int scoreB = 100 - scoreA;

        Map<String, Integer> result = new HashMap<>();
        result.put(participants.userA().getNickname(), scoreA);
        result.put(participants.userB().getNickname(), scoreB);
        return result;
    }

    /**
     * 정밀 분석: 승자, 점수, 코멘트를 포함한 상세 분석
     *
     * @param chatRoomId 채팅방 ID
     * @return 분석 결과
     */
    @Transactional(readOnly = true)
    public AnalysisResult analyzeDetailed(Long chatRoomId) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        ParticipantPair participants = getParticipants(chatRoom);
        List<ChatMessage> messages = getMessagesChronological(chatRoom);
        String formattedMessages = formatMessagesForAi(messages, participants);

//        String response = callOpenAi(DETAILED_SYSTEM_PROMPT, formattedMessages);
        String response = mockResponse();
        return parseDetailedResponse(response, participants);
    }

    // ========== 테스트용 메서드 ==========

    /**
     * mock response
     */

    private String mockResponse() {
        return """
                {
                    "winner": "원고",
                    "winner_logic_score": 85,
                    "winner_empathy_score": 90,
                    "judgment_comment": "원고는 논리적인 근거를 잘 제시했으며, 감정적으로도 상대방을 배려하는 태도를 보였습니다.",
                    "winner_reason": "원고는 구체적인 사례와 데이터를 활용하여 자신의 주장을 뒷받침했습니다.",
                    "loser_reason": "피고는 감정적인 비난을 사용하여 신뢰도를 떨어뜨렸습니다."
                }
                """;
    }

    /**
     * 퍼센트 분석 프롬프트 테스트: formattedMessages를 직접 받아서 AI 응답을 그대로 반환
     *
     * @param formattedMessages "A: 내용\nB: 내용" 형식의 메시지
     * @return AI 응답 원본
     */
    public String testPercentPrompt(String formattedMessages) {
        return callOpenAi(PERCENT_SYSTEM_PROMPT, formattedMessages);
    }

    /**
     * 정밀 분석 프롬프트 테스트: formattedMessages를 직접 받아서 AI 응답을 그대로 반환
     *
     * @param formattedMessages "A: 내용\nB: 내용" 형식의 메시지
     * @return AI 응답 원본
     */
    public String testDetailedPrompt(String formattedMessages) {
        return callOpenAi(DETAILED_SYSTEM_PROMPT, formattedMessages);
    }

    // ========== Helper Methods ==========

    private ChatRoom getChatRoom(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);
    }

    private ParticipantPair getParticipants(ChatRoom chatRoom) {
        List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoom(chatRoom);

        // PARTICIPANT 역할만 필터링
        List<ChatRoomMember> participants = members.stream()
                .filter(m -> m.getRole() == ChatRoomMember.MemberRole.PARTICIPANT)
                .toList();

        if (participants.size() < 2) {
            throw new InsufficientParticipantsException();
        }

        // User A = 채팅방 생성자, User B = 다른 참여자
        User creator = chatRoom.getCreator();
        User userA = null;
        User userB = null;

        for (ChatRoomMember member : participants) {
            if (member.getUser().getId().equals(creator.getId())) {
                userA = member.getUser();
            } else if (userB == null) {
                userB = member.getUser();
            }
        }

        if (userA == null || userB == null) {
            throw new InsufficientParticipantsException();
        }

        return new ParticipantPair(userA, userB);
    }

    private List<ChatMessage> getMessagesChronological(ChatRoom chatRoom) {
        return chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom);
    }

    private String formatMessagesForAi(List<ChatMessage> messages, ParticipantPair participants) {
        StringBuilder sb = new StringBuilder();

        for (ChatMessage message : messages) {
            String label = message.getSender().getId().equals(participants.userA().getId())
                    ? "원고" : "피고";
            sb.append(label)
                    .append(": ")
                    .append(message.getContent())
                    .append("\n");
        }

        return sb.toString().trim();
    }

    private String callOpenAi(String systemPrompt, String userMessage) {
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(MODEL)
                .messages(List.of(
                        Message.system(systemPrompt),
                        Message.user(userMessage)
                ))
                .temperature(0.3)
                .maxTokens(1000)
                .build();

        ChatCompletionResponse response = openAiClient.chatCompletion(request);
        String content = response.getContent();

        if (content == null || content.isBlank()) {
            throw new AnalysisParseException("AI 응답이 비어있습니다.");
        }

        return content;
    }

    private int parsePercentResponse(String response) {
        try {
            String json = extractJson(response);
            JsonNode node = objectMapper.readTree(json);
            return node.get("score").asInt();
        } catch (Exception e) {
            log.error("퍼센트 분석 응답 파싱 실패: {}", response, e);
            throw new AnalysisParseException(e.getMessage());
        }
    }

    private AnalysisResult parseDetailedResponse(String response, ParticipantPair participants) {
        try {
            String json = extractJson(response);
            JsonNode node = objectMapper.readTree(json);

            String winnerLabel = node.get("winner").asText();
            String winnerNickname = participants.getNickname(winnerLabel);

            return AnalysisResult.builder()
                    .winner(winnerNickname)
                    .winnerLogicScore(node.get("winner_logic_score").asInt())
                    .winnerEmpathyScore(node.get("winner_empathy_score").asInt())
                    .judgmentComment(node.get("judgment_comment").asText())
                    .winnerReason(node.get("winner_reason").asText())
                    .loserReason(node.get("loser_reason").asText())
                    .build();
        } catch (Exception e) {
            log.error("정밀 분석 응답 파싱 실패: {}", response, e);
            throw new AnalysisParseException(e.getMessage());
        }
    }

    private String extractJson(String response) {
        String trimmed = response.trim();

        // markdown 코드 블록 처리: ```json ... ``` 또는 ``` ... ```
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf('\n') + 1;
            int end = trimmed.lastIndexOf("```");
            if (start > 0 && end > start) {
                return trimmed.substring(start, end).trim();
            }
        }

        return trimmed;
    }

    // ========== Inner Classes ==========

    private record ParticipantPair(User userA, User userB) {
        public String getNickname(String label) {
            return "원고".equals(label) ? userA.getNickname() : userB.getNickname();
        }
    }
}
