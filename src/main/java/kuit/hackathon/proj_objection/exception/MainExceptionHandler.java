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
}
