package kuit.hackathon.proj_objection.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kuit.hackathon.proj_objection.annotation.LoginUser;
import kuit.hackathon.proj_objection.client.openai.OpenAiClient;
import kuit.hackathon.proj_objection.dto.response.BaseErrorResponse;
import kuit.hackathon.proj_objection.dto.response.BaseResponse;
import kuit.hackathon.proj_objection.entity.User;
import kuit.hackathon.proj_objection.service.OpenAiChatProcessor;
import org.springframework.web.bind.annotation.CrossOrigin;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "테스트", description = "서버 상태 및 세션 확인 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/test")
public class TestController {

    private final OpenAiClient openAiClient;
    private final OpenAiChatProcessor openAiChatProcessor;

    @Operation(summary = "서버 상태 확인", description = "서버가 정상 동작 중인지 확인합니다.")
    @ApiResponse(responseCode = "200", description = "서버 정상 동작")
    @GetMapping
    public BaseResponse<String> test() {
        return new BaseResponse<>("test successful");
    }

    @Operation(summary = "로그인 사용자 정보 조회", description = "현재 로그인된 사용자의 닉네임을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "로그인되지 않음",
                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class)))
    })
    @GetMapping("/me")
    public BaseResponse<String> testSession(
            @Parameter(hidden = true) @LoginUser User user
    ) {
        return new BaseResponse<>(user.getNickname());
    }

//    @Operation(summary = "퍼센트 분석 프롬프트 테스트", description = "formattedMessages를 직접 받아서 퍼센트 분석 프롬프트로 AI 응답을 테스트합니다.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "요청 성공"),
//            @ApiResponse(responseCode = "500", description = "OpenAI API 호출 실패",
//                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class)))
//    })
//    @PostMapping("/ai/percent")
//    public BaseResponse<String> testPercentPrompt(
//            @RequestBody PromptTestRequest request
//    ) {
//        String response = openAiChatProcessor.testPercentPrompt(request.formattedMessages());
//        return new BaseResponse<>(response);
//    }
//
//    @Operation(summary = "정밀 분석 프롬프트 테스트", description = "formattedMessages를 직접 받아서 정밀 분석 프롬프트로 AI 응답을 테스트합니다.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "요청 성공"),
//            @ApiResponse(responseCode = "500", description = "OpenAI API 호출 실패",
//                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class)))
//    })
//    @PostMapping("/ai/detailed")
//    public BaseResponse<String> testDetailedPrompt(
//            @RequestBody PromptTestRequest request
//    ) {
//        String response = openAiChatProcessor.testDetailedPrompt(request.formattedMessages());
//        return new BaseResponse<>(response);
//    }
//
//    @Schema(description = "프롬프트 테스트 요청")
//    public record PromptTestRequest(
//            @Schema(description = "원고와 피고의 대화 내용", example = "원고: 환경 보호가 중요합니다.\n피고: 경제 발전이 더 중요합니다.\n원고: 환경 없이는 지속 가능한 발전이 불가능합니다.")
//            String formattedMessages
//    ) {}

//    @Operation(summary = "OpenAI 단순 요청", description = "사용자 메시지만으로 OpenAI API를 호출합니다.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "요청 성공"),
//            @ApiResponse(responseCode = "500", description = "OpenAI API 호출 실패",
//                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class)))
//    })
//    @GetMapping("/ai")
//    public BaseResponse<String> testOpenAi(
//            @Parameter(description = "질문 내용", example = "안녕하세요?")
//            @RequestParam String message
//    ) {
//        ChatCompletionRequest request = ChatCompletionRequest.of("gpt-4o", message);
//        String response = openAiClient.chatCompletion(request).getContent();
//        return new BaseResponse<>(response);
//    }
//
//    @Operation(summary = "OpenAI 시스템 프롬프트 포함 요청", description = "시스템 프롬프트와 사용자 메시지로 OpenAI API를 호출합니다.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "요청 성공"),
//            @ApiResponse(responseCode = "500", description = "OpenAI API 호출 실패",
//                    content = @Content(schema = @Schema(implementation = BaseErrorResponse.class)))
//    })
//    @PostMapping("/ai")
//    public BaseResponse<String> testOpenAiWithSystem(
//            @RequestBody OpenAiTestRequest request
//    ) {
//        ChatCompletionRequest chatRequest = ChatCompletionRequest.builder()
//                .model("gpt-4o")
//                .messages(List.of(
//                        Message.system(request.systemPrompt()),
//                        Message.user(request.userMessage())
//                ))
//                .build();
//        String response = openAiClient.chatCompletion(chatRequest).getContent();
//        return new BaseResponse<>(response);
//    }
//
//    @Schema(description = "OpenAI 테스트 요청")
//    public record OpenAiTestRequest(
//            @Schema(description = "시스템 프롬프트", example = "당신은 친절한 AI 어시스턴트입니다.")
//            String systemPrompt,
//            @Schema(description = "사용자 메시지", example = "안녕하세요?")
//            String userMessage
//    ) {}
}

