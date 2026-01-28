package com.example.sso.service;

import com.example.sso.oauth.OAuthReq;
import com.example.sso.session.ChannelUserDetails;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthorizationCodeService {

  public String issueCode(OAuthReq req, ChannelUserDetails user) {
    // TODO: Store code->data in Geode/DB for /token exchange.
    return UUID.randomUUID().toString();
  }
}
