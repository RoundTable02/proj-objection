package kuit.hackathon.proj_objection.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kuit.hackathon.proj_objection.annotation.LoginUser;
import kuit.hackathon.proj_objection.dto.response.BaseErrorResponse;
import kuit.hackathon.proj_objection.dto.response.BaseResponse;
import kuit.hackathon.proj_objection.dto.response.ChatPollResponseDto;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.service.ChatPollService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "채팅 폴링", description = "채팅방 폴링 API")
@RequiredArgsConstructor
@RestController
public class ChatPollController {

    private final ChatPollService chatPollService;

    @Operation(
            summary = "채팅방 폴링",
            description = "채팅방의 새로운 메시지, 상태, 승률을 조회합니다. lastMessageId 이후의 메시지만 반환됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "폴링 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "채팅방 멤버가 아님",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class)))
    })
    @GetMapping("/chat/poll")
    public BaseResponse<ChatPollResponseDto> poll(
            @Parameter(description = "채팅방 ID", example = "1", required = true)
            @RequestParam Long chatRoomId,
            @Parameter(description = "마지막으로 받은 메시지 ID (null이면 모든 메시지 조회)", example = "123")
            @RequestParam(required = false) Long lastMessageId,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        ChatPollResponseDto response = chatPollService.poll(chatRoomId, lastMessageId, user);
        return new BaseResponse<>(response);
    }
}
