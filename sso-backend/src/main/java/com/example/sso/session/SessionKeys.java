package com.example.sso.session;

public final class SessionKeys {
  private SessionKeys() {}

  public static final String AUTHZ_REQ_MAP = "AUTHZ_REQ_MAP";           // Map<String, OAuthReq>
  public static final String CHANNEL_USER_MAP = "CHANNEL_USER_MAP";     // Map<String, ChannelUserDetails>
  public static final String LOGOUT_BLOCK_MAP = "LOGOUT_BLOCK_MAP";     // Map<String, Boolean>
}
