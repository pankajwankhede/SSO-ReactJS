package com.example.sso.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class LoginServiceClient {

  public LoginResult authenticate(String realm, String clientId, String username, String password) {
    // TODO: Replace with real downstream login service call per your environment.
    // This is a dummy implementation so project runs.
    LoginResult r = new LoginResult();
    r.success = true;
    r.userId = "U-" + username;
    r.username = username;
    r.roles = List.of("USER");
    r.attributes = Map.of("realm", realm, "clientId", clientId);
    return r;
  }

  public static class LoginResult {
    public boolean success;
    public String userId;
    public String username;
    public List<String> roles;
    public Map<String, Object> attributes;
    public String errorMessage;
  }
}
