package kuit.hackathon.proj_objection.service;

import kuit.hackathon.proj_objection.dto.common.ChatPollMessageDto;
import kuit.hackathon.proj_objection.dto.common.ChatRoomStatusCache;
import kuit.hackathon.proj_objection.dto.response.ChatPollResponseDto;
import kuit.hackathon.proj_objection.entity.ChatMessage;
import kuit.hackathon.proj_objection.entity.ChatRoom;
import kuit.hackathon.proj_objection.entity.ChatRoomMember;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.exception.ChatRoomMemberNotFoundException;
import kuit.hackathon.proj_objection.exception.ChatRoomNotFoundException;
import kuit.hackathon.proj_objection.repository.ChatMessageRepository;
import kuit.hackathon.proj_objection.repository.ChatRoomMemberRepository;
import kuit.hackathon.proj_objection.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatPollService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomCacheService chatRoomCacheService;

    @Transactional(readOnly = true)
    public ChatPollResponseDto poll(Long chatRoomId, Long lastMessageId, User user) {
        // 1. 보안: 채팅방 존재 확인 + 멤버십 검증 (항상 DB 조회)
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);
        chatRoomMemberRepository.findByChatRoomAndUser(chatRoom, user)
                .orElseThrow(ChatRoomMemberNotFoundException::new);

        Long effectiveLastMessageId = (lastMessageId != null) ? lastMessageId : 0L;

        try {
            // 2. Redis에서 lastMessageId 조회
            Long redisLastMessageId = chatRoomCacheService.getLastMessageId(chatRoomId);

            if (redisLastMessageId != null) {
                // 2-A. 캐시 HIT: Redis 기반 폴링
                return pollWithCache(chatRoom, effectiveLastMessageId, redisLastMessageId);
            } else {
                // 2-B. 캐시 MISS: DB 조회 후 캐시 생성
                return pollWithoutCache(chatRoom, effectiveLastMessageId);
            }
        } catch (Exception e) {
            // 3. Redis 장애 시 자동 Fallback to DB
            log.warn("Redis error during poll for room {}, falling back to DB: {}",
                     chatRoomId, e.getMessage());
            return pollWithoutCache(chatRoom, effectiveLastMessageId);
        }
    }

    /**
     * 캐시 HIT 시: Redis에서 데이터 조회, 새 메시지만 DB에서 fetch
     */
    private ChatPollResponseDto pollWithCache(ChatRoom chatRoom, Long clientLastMessageId,
                                              Long redisLastMessageId) {
        List<ChatPollMessageDto> messages = List.of();

        // 새 메시지가 있는 경우에만 DB 조회
        if (redisLastMessageId > clientLastMessageId) {
            List<ChatMessage> newMessages = chatMessageRepository
                    .findMessagesWithSender(chatRoom, clientLastMessageId);  // JOIN FETCH로 N+1 해결

            messages = newMessages.stream()
                    .map(msg -> new ChatPollMessageDto(
                            msg.getId(),
                            msg.getSender().getNickname(),
                            msg.getContent(),
                            msg.getCreatedAt()
                    ))
                    .collect(Collectors.toList());
        }

        // Redis에서 percent와 status 조회
        Map<String, Integer> percent = chatRoomCacheService.getPercent(chatRoom.getId());
        ChatRoomStatusCache statusCache = chatRoomCacheService.getStatus(chatRoom.getId());

        // 부분 캐시 미스 → DB 재조회하여 캐시 복구
        if (percent == null || statusCache == null) {
            log.warn("Partial cache miss for room {}, rebuilding cache", chatRoom.getId());
            return pollWithoutCache(chatRoom, clientLastMessageId);
        }

        return ChatPollResponseDto.builder()
                .messages(messages)
                .chatRoomStatus(statusCache.getStatus())
                .finishRequestNickname(statusCache.getFinishRequestNickname())
                .percent(percent)
                .build();
    }

    /**
     * 캐시 MISS 시: DB에서 모든 데이터 조회 후 캐시 생성
     */
    private ChatPollResponseDto pollWithoutCache(ChatRoom chatRoom, Long clientLastMessageId) {
        // 기존 로직: DB에서 모든 데이터 조회 (JOIN FETCH 사용으로 N+1 해결)
        List<ChatMessage> messages = chatMessageRepository
                .findMessagesWithSender(chatRoom, clientLastMessageId);

        // PARTICIPANT만 필터링하여 percent 계산
        List<ChatRoomMember> allMembers = chatRoomMemberRepository.findByChatRoom(chatRoom);
        Map<String, Integer> percent = allMembers.stream()
                .filter(member -> member.getRole() == ChatRoomMember.MemberRole.PARTICIPANT)
                .collect(Collectors.toMap(
                        member -> member.getUser().getNickname(),
                        ChatRoomMember::getPercent
                ));

        String finishRequestNickname = (chatRoom.getExitRequester() != null)
                ? chatRoom.getExitRequester().getNickname()
                : null;

        // Redis 캐시 생성 (비동기 아님, but 에러 시 무시)
        try {
            if (!messages.isEmpty()) {
                Long lastMsgId = messages.get(messages.size() - 1).getId();
                chatRoomCacheService.setLastMessageId(chatRoom.getId(), lastMsgId);
            } else {
                // 메시지가 하나도 없으면 0으로 캐싱 (첫 폴링 케이스)
                chatRoomCacheService.setLastMessageId(chatRoom.getId(), 0L);
            }
            chatRoomCacheService.setPercent(chatRoom.getId(), percent);
            chatRoomCacheService.setStatus(chatRoom.getId(), chatRoom.getStatus(), finishRequestNickname);
        } catch (Exception e) {
            log.warn("Failed to create cache for room {}: {}", chatRoom.getId(), e.getMessage());
            // 캐시 생성 실패해도 응답은 정상 반환
        }

        List<ChatPollMessageDto> messageDtos = messages.stream()
                .map(msg -> new ChatPollMessageDto(
                        msg.getId(),
                        msg.getSender().getNickname(),
                        msg.getContent(),
                        msg.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return ChatPollResponseDto.builder()
                .messages(messageDtos)
                .chatRoomStatus(chatRoom.getStatus())
                .finishRequestNickname(finishRequestNickname)
                .percent(percent)
                .build();
    }
}
