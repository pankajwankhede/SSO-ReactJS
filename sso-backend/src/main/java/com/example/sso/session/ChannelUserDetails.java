package com.example.sso.session;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class ChannelUserDetails implements Serializable {
  private final String realm;
  private final String userId;
  private final String username;
  private final List<String> roles;
  private final Map<String, Object> attributes;
  private final Instant authenticatedAt;

  public ChannelUserDetails(String realm, String userId, String username,
                            List<String> roles, Map<String, Object> attributes,
                            Instant authenticatedAt) {
    this.realm = realm;
    this.userId = userId;
    this.username = username;
    this.roles = roles;
    this.attributes = attributes;
    this.authenticatedAt = authenticatedAt;
  }

  public String getRealm() { return realm; }
  public String getUserId() { return userId; }
  public String getUsername() { return username; }
  public List<String> getRoles() { return roles; }
  public Map<String, Object> getAttributes() { return attributes; }
  public Instant getAuthenticatedAt() { return authenticatedAt; }
}
