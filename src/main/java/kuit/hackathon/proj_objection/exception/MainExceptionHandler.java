package kuit.hackathon.proj_objection.exception;

import kuit.hackathon.proj_objection.dto.BaseErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class MainExceptionHandler {
    @ExceptionHandler({UserNotFoundException.class})
    public BaseErrorResponse handle_UserNotFoundException(UserNotFoundException exception){
        log.error("MainExceptionHandler.handle_UserNotFoundException <{}> {}", exception.getMessage(), exception);

        return BaseErrorResponse.of(exception);
    }

    @ExceptionHandler({LoginException.class})
    public BaseErrorResponse handle_LoginException(LoginException exception){
        log.error("MainExceptionHandler.handle_LoginException <{}> {}", exception.getMessage(), exception);

        return BaseErrorResponse.of(exception);
    }

    @ExceptionHandler({ChatRoomNotFoundException.class})
    public BaseErrorResponse handle_ChatRoomNotFoundException(ChatRoomNotFoundException exception) {
        log.error("MainExceptionHandler.handle_ChatRoomNotFoundException <{}> {}", exception.getMessage(), exception);
        return BaseErrorResponse.of(exception);
    }

    @ExceptionHandler({InvalidInviteCodeException.class})
    public BaseErrorResponse handle_InvalidInviteCodeException(InvalidInviteCodeException exception) {
        log.error("MainExceptionHandler.handle_InvalidInviteCodeException <{}> {}", exception.getMessage(), exception);
        return BaseErrorResponse.of(exception);
    }

    @ExceptionHandler({AlreadyJoinedChatRoomException.class})
    public BaseErrorResponse handle_AlreadyJoinedChatRoomException(AlreadyJoinedChatRoomException exception) {
        log.error("MainExceptionHandler.handle_AlreadyJoinedChatRoomException <{}> {}", exception.getMessage(), exception);
        return BaseErrorResponse.of(exception);
    }

    @ExceptionHandler({ChatRoomMemberNotFoundException.class})
    public BaseErrorResponse handle_ChatRoomMemberNotFoundException(ChatRoomMemberNotFoundException exception) {
        log.error("MainExceptionHandler.handle_ChatRoomMemberNotFoundException <{}> {}", exception.getMessage(), exception);
        return BaseErrorResponse.of(exception);
    }

    @ExceptionHandler({MessageSendPermissionDeniedException.class})
    public BaseErrorResponse handle_MessageSendPermissionDeniedException(MessageSendPermissionDeniedException exception) {
        log.error("MainExceptionHandler.handle_MessageSendPermissionDeniedException <{}> {}", exception.getMessage(), exception);
        return BaseErrorResponse.of(exception);
    }

    @ExceptionHandler({ChatRoomClosedException.class})
    public BaseErrorResponse handle_ChatRoomClosedException(ChatRoomClosedException exception) {
        log.error("MainExceptionHandler.handle_ChatRoomClosedException <{}> {}", exception.getMessage(), exception);
        return BaseErrorResponse.of(exception);
    }

    @ExceptionHandler({ExitRequestPermissionDeniedException.class})
    public BaseErrorResponse handle_ExitRequestPermissionDeniedException(ExitRequestPermissionDeniedException exception) {
        log.error("MainExceptionHandler.handle_ExitRequestPermissionDeniedException <{}> {}", exception.getMessage(), exception);
        return BaseErrorResponse.of(exception);
    }

    @ExceptionHandler({ExitDecisionPermissionDeniedException.class})
    public BaseErrorResponse handle_ExitDecisionPermissionDeniedException(ExitDecisionPermissionDeniedException exception) {
        log.error("MainExceptionHandler.handle_ExitDecisionPermissionDeniedException <{}> {}", exception.getMessage(), exception);
        return BaseErrorResponse.of(exception);
    }

    @ExceptionHandler({NoExitRequestException.class})
    public BaseErrorResponse handle_NoExitRequestException(NoExitRequestException exception) {
        log.error("MainExceptionHandler.handle_NoExitRequestException <{}> {}", exception.getMessage(), exception);
        return BaseErrorResponse.of(exception);
    }


    @ExceptionHandler({OpenAiApiException.class})
    public BaseErrorResponse handle_OpenAiApiException(OpenAiApiException exception){
        log.error("MainExceptionHandler.handle_OpenAiApiException <{}> {}", exception.getMessage(), exception);

        return BaseErrorResponse.of(exception);
    }

    @ExceptionHandler({InsufficientParticipantsException.class})
    public BaseErrorResponse handle_InsufficientParticipantsException(InsufficientParticipantsException exception) {
        log.error("MainExceptionHandler.handle_InsufficientParticipantsException <{}> {}", exception.getMessage(), exception);
        return BaseErrorResponse.of(exception);
    }

    @ExceptionHandler({AnalysisParseException.class})
    public BaseErrorResponse handle_AnalysisParseException(AnalysisParseException exception) {
        log.error("MainExceptionHandler.handle_AnalysisParseException <{}> {}", exception.getMessage(), exception);
        return BaseErrorResponse.of(exception);
    }

    @ExceptionHandler({FinalJudgementNotFoundException.class})
    public BaseErrorResponse handle_FinalJudgementNotFoundException(FinalJudgementNotFoundException exception) {
        log.error("MainExceptionHandler.handle_FinalJudgementNotFoundException <{}> {}", exception.getMessage(), exception);
        return BaseErrorResponse.of(exception);
    }
}
