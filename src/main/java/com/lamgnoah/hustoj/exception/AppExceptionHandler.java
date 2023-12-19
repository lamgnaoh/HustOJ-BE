package com.lamgnoah.hustoj.exception;

import com.lamgnoah.hustoj.entity.ErrorResponse;
import com.lamgnoah.hustoj.domain.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class AppExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationErrors(HttpServletRequest request,MethodArgumentNotValidException ex) {
    Map<String, Object> responseBody = new LinkedHashMap<>();
    String errorMessage = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();
    responseBody.put("status" , HttpStatus.BAD_REQUEST.value());
    responseBody.put("url" , request.getRequestURL().toString());
    responseBody.put("code", "-1");
    responseBody.put("message" , errorMessage);
    return new ResponseEntity<>(responseBody, new HttpHeaders(), HttpStatus.BAD_REQUEST);
  }


  @ExceptionHandler(AppException.class)
  @ResponseBody
  public ResponseEntity<ErrorResponse> appErrorHandler(HttpServletRequest request , AppException e){
    ErrorResponse resp = new ErrorResponse();
    resp.setCode(e.getError().getCode());
    resp.setMessage(e.getError().getMessage());
    resp.setUrl(request.getRequestURL().toString());
    return ResponseEntity.status(e.getError().getStatus()).body(resp);
  }

  @ExceptionHandler(value = {Exception.class})
  @ResponseBody
  public ResponseEntity<ErrorResponse> generalErrorHandler(HttpServletRequest req, Exception e) {
    log.error("Uncaught Exception", e);
    ErrorResponse resp = new ErrorResponse();
    resp.setMessage("System Error");
    resp.setCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode());
    resp.setUrl(req.getRequestURL().toString());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
  }
}
