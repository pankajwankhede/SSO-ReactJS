package com.example.sso.web.dto;

import java.util.Map;

public record ApiResponse(
    String status,     // OK / FAIL
    String action,     // LOGIN_REQUIRED / REDIRECT / NONE
    String realm,
    String clientId,
    String redirectTo,
    String message,
    Map<String, Object> data
) {
  public static ApiResponse ok(String realm, String clientId, Map<String, Object> data) {
    return new ApiResponse("OK", "NONE", realm, clientId, null, null, data);
  }

  public static ApiResponse loginRequired(String realm, String clientId) {
    return new ApiResponse("OK", "LOGIN_REQUIRED", realm, clientId, null, null, null);
  }

  public static ApiResponse redirect(String realm, String clientId, String redirectTo) {
    return new ApiResponse("OK", "REDIRECT", realm, clientId, redirectTo, null, null);
  }

  public static ApiResponse fail(String realm, String clientId, String message) {
    return new ApiResponse("FAIL", "NONE", realm, clientId, null, message, null);
  }
}
