package kuit.hackathon.proj_objection.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kuit.hackathon.proj_objection.annotation.LoginUser;
import kuit.hackathon.proj_objection.dto.BaseErrorResponse;
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

@Tag(name = "채팅 메시지", description = "채팅 메시지 전송/조회 API")
@RequiredArgsConstructor
@RestController
@CrossOrigin(originPatterns = "*", allowCredentials = "true")       // CORS
public class ChatMessageController {
    private final ChatMessageService chatMessageService;
    private final UserRepository userRepository;

    @Operation(summary = "메시지 전송", description = "채팅방에 메시지를 전송합니다. PARTICIPANT만 메시지 전송이 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메시지 전송 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "메시지 전송 권한 없음 (OBSERVER는 전송 불가)",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class)))
    })
    @PostMapping("/chat/room/{chatRoomId}/message")
    public BaseResponse<ChatMessageDto> sendMessage(
            @Parameter(description = "채팅방 ID", example = "1") @PathVariable Long chatRoomId,
            @RequestBody SendMessageRequestDto request,
            @Parameter(hidden = true) @RequestHeader(value = "X-SESSION-TOKEN", required = false) String sessionToken,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        ChatMessageDto response = chatMessageService.sendMessage(chatRoomId, user, request.getContent());
        return new BaseResponse<>(response);
    }

    @Operation(summary = "메시지 전체 목록 조회", description = "채팅방의 모든 메시지를 시간 역순으로 조회합니다. 채팅방 멤버만 조회 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메시지 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "채팅방 멤버가 아님",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class)))
    })
    @GetMapping("/chat/room/{chatRoomId}/messages")
    public BaseResponse<List<ChatMessageListDto>> getChatMessages(
            @Parameter(description = "채팅방 ID", example = "1") @PathVariable Long chatRoomId,
            @Parameter(hidden = true) @RequestHeader(value = "X-SESSION-TOKEN", required = false) String sessionToken,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        List<ChatMessageListDto> messages = chatMessageService.getChatMessages(chatRoomId, user);
        return new BaseResponse<>(messages);
    }


    @Operation(summary = "메시지 마지막 메세지 이후 목록 조회", description = "채팅방의 모든 메시지를 시간 역순으로 조회합니다. 채팅방 멤버만 조회 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메시지 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "채팅방 멤버가 아님",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class)))
    })
    @GetMapping("/chat/poll")
    public BaseResponse<List<ChatMessageListDto>> getChatMessagesAfterLastMessage(
            @RequestParam Long chatRoomId,
            @RequestParam(required = false, defaultValue = "0") Long lastMessageId,
            @Parameter(hidden = true) @RequestHeader(value = "X-SESSION-TOKEN", required = false) String sessionToken,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        List<ChatMessageListDto> messages = chatMessageService.getChatMessagesAfterLastMessage(chatRoomId, lastMessageId, user);
        return new BaseResponse<>(messages);
    }
}
