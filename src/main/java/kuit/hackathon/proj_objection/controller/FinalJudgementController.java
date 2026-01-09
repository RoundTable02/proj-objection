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
import kuit.hackathon.proj_objection.dto.response.FinalJudgementResponseDto;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.service.FinalJudgementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "최종 판결문", description = "최종 판결문 조회 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/chat/room")
public class FinalJudgementController {

    private final FinalJudgementService finalJudgementService;

    @Operation(
            summary = "최종 판결문 조회",
            description = "채팅방의 최종 판결문을 조회합니다. 채팅방 멤버만 조회 가능합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "최종 판결문 조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "채팅방 멤버가 아님",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "채팅방 또는 판결문을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class)))
    })
    @GetMapping("/{chatRoomId}/final-judgement")
    public BaseResponse<FinalJudgementResponseDto> getFinalJudgement(
            @Parameter(description = "채팅방 ID", example = "1") @PathVariable Long chatRoomId,
            @Parameter(hidden = true) @LoginUser User user
    ) {
        FinalJudgementResponseDto response = finalJudgementService.getByChatRoomId(chatRoomId, user);
        return new BaseResponse<>(response);
    }
}
