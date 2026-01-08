package kuit.hackathon.proj_objection.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kuit.hackathon.proj_objection.annotation.LoginUser;
import kuit.hackathon.proj_objection.dto.*;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "채팅방", description = "채팅방 생성/입장 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/chat/room")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")       // CORS
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    @Operation(summary = "채팅방 생성", description = "새로운 채팅방을 생성합니다. 생성자는 자동으로 PARTICIPANT로 입장됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채팅방 생성 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class)))
    })
    @PostMapping("/create")
    public BaseResponse<CreateChatRoomResponseDto> createChatRoom(
            @RequestBody CreateChatRoomRequestDto request,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        CreateChatRoomResponseDto response = chatRoomService.createChatRoom(user, request.getReward());
        return new BaseResponse<>(response);
    }

    @Operation(summary = "채팅방 입장", description = "초대 코드를 사용하여 채팅방에 입장합니다. participantCode로 입장 시 PARTICIPANT, observerCode로 입장 시 OBSERVER 역할이 부여됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채팅방 입장 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 초대 코드",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인 필요",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 입장한 채팅방",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class)))
    })
    @PostMapping("/join")
    public BaseResponse<JoinChatRoomResponseDto> joinChatRoom(
            @RequestBody JoinChatRoomRequestDto request,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        JoinChatRoomResponseDto response = chatRoomService.joinChatRoom(request.getInviteCode(), user);
        return new BaseResponse<>(response);
    }
}
