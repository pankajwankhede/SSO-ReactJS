package com.example.sso.oauth;

import java.io.Serializable;

public class OAuthReq implements Serializable {
  public String clientId;
  public String redirectUrl;
  public String responseType;
  public String scope;
  public String state;
  public String realm;

  public OAuthReq() {}

  public OAuthReq(String clientId, String redirectUrl, String responseType,
                  String scope, String state, String realm) {
    this.clientId = clientId;
    this.redirectUrl = redirectUrl;
    this.responseType = responseType;
    this.scope = scope;
    this.state = state;
    this.realm = realm;
  }
}
