package kuit.hackathon.proj_objection.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseException extends RuntimeException{
  protected int code;
  protected String errorMessage;

  public BaseException(HttpStatus code, String errorMessage) {
    super(errorMessage);
    this.code = code.value();
    this.errorMessage = errorMessage;
  }
}

