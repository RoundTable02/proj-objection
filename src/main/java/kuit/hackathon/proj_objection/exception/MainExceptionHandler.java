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

    @ExceptionHandler({OpenAiApiException.class})
    public BaseErrorResponse handle_OpenAiApiException(OpenAiApiException exception){
        log.error("MainExceptionHandler.handle_OpenAiApiException <{}> {}", exception.getMessage(), exception);

        return BaseErrorResponse.of(exception);
    }
}
