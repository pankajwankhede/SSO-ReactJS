package com.example.sso.service;

import com.example.sso.config.SsoProperties;
import com.example.sso.session.ChannelUserDetails;
import com.example.sso.session.ChannelUserSessionStore;
import com.example.sso.session.LogoutBlockSessionStore;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Service
public class SessionSharingService {

  private final SsoProperties ssoProperties;
  private final ChannelUserSessionStore channelUserStore;
  private final LogoutBlockSessionStore logoutBlockStore;

  public SessionSharingService(SsoProperties ssoProperties,
                               ChannelUserSessionStore channelUserStore,
                               LogoutBlockSessionStore logoutBlockStore) {
    this.ssoProperties = ssoProperties;
    this.channelUserStore = channelUserStore;
    this.logoutBlockStore = logoutBlockStore;
  }

  /**
   * Try to get an existing user for target realm:
   * 1) If already logged in for target realm -> return it
   * 2) If logout-block is set for target realm -> return null (force manual login)
   * 3) Else, check allowFrom list and reuse first available realm session
   *    -> auto-create target realm user session and return it
   */
  public ChannelUserDetails resolveOrShare(HttpSession session, String targetRealm) {
    if (session == null || targetRealm == null || targetRealm.isBlank()) return null;

    String realm = targetRealm.toUpperCase();

    // 1) already logged in
    ChannelUserDetails existing = channelUserStore.get(session, realm);
    if (existing != null) return existing;

    // 2) Option B: logout-block prevents auto-login by sharing
    if (logoutBlockStore.isBlocked(session, realm)) {
      return null;
    }

    // 3) check sharing config for target realm
    SsoProperties.SessionSharing.Rule rule = ssoProperties.sharingRule(realm);
    Set<String> allowedSources = rule.allowFromUpper();

    // no sharing allowed
    if (allowedSources.isEmpty()) return null;

    // find the first source realm that has a user session
    for (String sourceRealm : allowedSources) {
      ChannelUserDetails src = channelUserStore.get(session, sourceRealm);
      if (src != null) {
        // auto create new realm session (do NOT overwrite source realm)
        ChannelUserDetails auto = new ChannelUserDetails(
            realm,
            src.getUserId(),
            src.getUsername(),
            src.getRoles(),
            Map.of(
                "autoLogin", true,
                "sharedFrom", sourceRealm
            ),
            Instant.now()
        );

        channelUserStore.put(session, realm, auto);
        return auto;
      }
    }

    return null;
  }
}
