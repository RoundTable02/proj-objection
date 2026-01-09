package kuit.hackathon.proj_objection.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kuit.hackathon.proj_objection.dto.common.ChatRoomStatusCache;
import kuit.hackathon.proj_objection.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatRoomCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final Duration TTL = Duration.ofHours(1);

    // Key generators
    private String keyLastMessageId(Long chatRoomId) {
        return "chat:room:" + chatRoomId + ":lastMessageId";
    }

    private String keyPercent(Long chatRoomId) {
        return "chat:room:" + chatRoomId + ":percent";
    }

    private String keyStatus(Long chatRoomId) {
        return "chat:room:" + chatRoomId + ":status";
    }

    // GET methods
    public Long getLastMessageId(Long chatRoomId) {
        try {
            String value = redisTemplate.opsForValue().get(keyLastMessageId(chatRoomId));
            return (value != null) ? Long.parseLong(value) : null;
        } catch (Exception e) {
            log.warn("Failed to get lastMessageId from Redis for room {}: {}", chatRoomId, e.getMessage());
            return null;
        }
    }

    public Map<String, Integer> getPercent(Long chatRoomId) {
        try {
            String json = redisTemplate.opsForValue().get(keyPercent(chatRoomId));
            if (json == null) return null;
            return objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {});
        } catch (Exception e) {
            log.warn("Failed to get percent from Redis for room {}: {}", chatRoomId, e.getMessage());
            return null;
        }
    }

    public ChatRoomStatusCache getStatus(Long chatRoomId) {
        try {
            String json = redisTemplate.opsForValue().get(keyStatus(chatRoomId));
            if (json == null) return null;
            return objectMapper.readValue(json, ChatRoomStatusCache.class);
        } catch (Exception e) {
            log.warn("Failed to get status from Redis for room {}: {}", chatRoomId, e.getMessage());
            return null;
        }
    }

    // SET methods
    public void setLastMessageId(Long chatRoomId, Long messageId) {
        try {
            redisTemplate.opsForValue().set(
                keyLastMessageId(chatRoomId),
                String.valueOf(messageId),
                TTL
            );
            log.debug("Cached lastMessageId {} for room {}", messageId, chatRoomId);
        } catch (Exception e) {
            log.warn("Failed to cache lastMessageId for room {}: {}", chatRoomId, e.getMessage());
        }
    }

    public void setPercent(Long chatRoomId, Map<String, Integer> percent) {
        try {
            String json = objectMapper.writeValueAsString(percent);
            redisTemplate.opsForValue().set(keyPercent(chatRoomId), json, TTL);
            log.debug("Cached percent for room {}: {}", chatRoomId, percent);
        } catch (Exception e) {
            log.warn("Failed to cache percent for room {}: {}", chatRoomId, e.getMessage());
        }
    }

    public void setStatus(Long chatRoomId, ChatRoom.RoomStatus status, String finishRequestNickname) {
        try {
            ChatRoomStatusCache cache = new ChatRoomStatusCache(status, finishRequestNickname);
            String json = objectMapper.writeValueAsString(cache);
            redisTemplate.opsForValue().set(keyStatus(chatRoomId), json, TTL);
            log.debug("Cached status for room {}: {}", chatRoomId, status);
        } catch (Exception e) {
            log.warn("Failed to cache status for room {}: {}", chatRoomId, e.getMessage());
        }
    }

    // DELETE method (for cache invalidation)
    public void deleteAll(Long chatRoomId) {
        try {
            redisTemplate.delete(List.of(
                keyLastMessageId(chatRoomId),
                keyPercent(chatRoomId),
                keyStatus(chatRoomId)
            ));
            log.debug("Deleted all cache for room {}", chatRoomId);
        } catch (Exception e) {
            log.warn("Failed to delete cache for room {}: {}", chatRoomId, e.getMessage());
        }
    }
}
