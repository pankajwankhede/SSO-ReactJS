package com.example.sso.web;

import com.example.sso.web.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse> illegalArg(IllegalArgumentException ex) {
    return ResponseEntity.badRequest().body(ApiResponse.fail(null, null, ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse> generic(Exception ex) {
    return ResponseEntity.internalServerError().body(ApiResponse.fail(null, null, ex.getMessage()));
  }
}
