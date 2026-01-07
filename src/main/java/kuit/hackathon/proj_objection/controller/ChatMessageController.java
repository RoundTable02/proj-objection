package kuit.hackathon.proj_objection.controller;

import kuit.hackathon.proj_objection.annotation.LoginUser;
import kuit.hackathon.proj_objection.dto.BaseResponse;
import kuit.hackathon.proj_objection.dto.ChatMessageDto;
import kuit.hackathon.proj_objection.dto.ChatMessageListDto;
import kuit.hackathon.proj_objection.dto.SendMessageRequestDto;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.exception.UserNotFoundException;
import kuit.hackathon.proj_objection.repository.UserRepository;
import kuit.hackathon.proj_objection.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ChatMessageController {
    private final ChatMessageService chatMessageService;
    private final UserRepository userRepository;

    // REST API: 메시지 전송
    @PostMapping("/chat/room/{chatRoomId}/message")
    public BaseResponse<ChatMessageDto> sendMessage(
            @PathVariable Long chatRoomId,
            @RequestBody SendMessageRequestDto request,
            @RequestHeader(value = "X-SESSION-TOKEN", required = false) String sessionToken,
            @LoginUser User user
    ) {
        ChatMessageDto response = chatMessageService.sendMessage(chatRoomId, user, request.getContent());
        return new BaseResponse<>(response);
    }

    // REST API: 메시지 목록 조회 (시간 역순)
    @GetMapping("/chat/room/{chatRoomId}/messages")
    public BaseResponse<List<ChatMessageListDto>> getChatMessages(
            @PathVariable Long chatRoomId,
            @RequestHeader(value = "X-SESSION-TOKEN", required = false) String sessionToken,
            @LoginUser User user
    ) {
        List<ChatMessageListDto> messages = chatMessageService.getChatMessages(chatRoomId, user);
        return new BaseResponse<>(messages);
    }

    // WebSocket: 메시지 전송 (STOMP)
    @MessageMapping("/chatroom/{chatRoomId}")
    public void sendMessageViaWebSocket(
            @DestinationVariable Long chatRoomId,
            @Payload SendMessageRequestDto request,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        // WebSocket 세션에서 userId 추출
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        if (userId == null) {
            throw new UserNotFoundException("User not logged in");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // 메시지 전송 (서비스에서 브로드캐스트 처리)
        chatMessageService.sendMessage(chatRoomId, user, request.getContent());
    }
}
