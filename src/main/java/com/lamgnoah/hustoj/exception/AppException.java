package com.lamgnoah.hustoj.exception;


import com.lamgnoah.hustoj.domain.enums.ErrorCode;
import lombok.Data;

@Data
public class AppException extends RuntimeException{

  private final ErrorCode error ;
  public AppException(ErrorCode error) {
    super();
    this.error = error;
  }
}
