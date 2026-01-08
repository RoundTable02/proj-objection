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
            @Parameter(hidden = true) @LoginUser User user
    ) {
        CreateChatRoomResponseDto response = chatRoomService.createChatRoom(user);
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

    // 종료 요청
    @Operation(
            summary = "채팅방 판결(종료) 요청",
            description = "채팅방에서 판결(종료)을 요청합니다. 요청이 접수되면 종료 절차가 진행되며, 이미 종료 요청이 존재하거나 권한/상태가 맞지 않으면 실패할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채팅방 종료 요청 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청(채팅방 상태/요청 상태가 유효하지 않음)",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 필요",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음(해당 채팅방 멤버가 아님 또는 종료 요청 권한 없음)",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "채팅방을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 종료 요청이 존재함(중복 요청)",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class))
            )
    })
    @PostMapping("/{chatRoomId}/exit/request")
    public BaseResponse<ExitRequestResponseDto> requestExit(
            @PathVariable Long chatRoomId,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        ExitRequestResponseDto response = chatRoomService.requestExit(chatRoomId, user);
        return new BaseResponse<>(response);
    }

    // 종료 수락/거절
    @Operation(
            summary = "채팅방 판결(종료) 요청 결정",
            description = "채팅방 판결(종료) 요청에 대해 승인 또는 거절을 결정합니다. approve 값이 true이면 종료가 승인되고, false이면 종료가 거절됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "종료 요청 결정 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청(이미 처리된 요청이거나 유효하지 않은 상태)",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 필요",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음(종료 요청을 결정할 권한이 없음)",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "채팅방 또는 종료 요청을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 결정된 종료 요청",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class))
            )
    })
    @PostMapping("/{chatRoomId}/exit/decide")
    public BaseResponse<ExitDecisionResponseDto> decideExit(
            @PathVariable Long chatRoomId,
            @RequestBody ExitDecisionRequestDto request,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        ExitDecisionResponseDto response = chatRoomService.decideExit(chatRoomId, user, request.getApprove());
        return new BaseResponse<>(response);
    }
}
