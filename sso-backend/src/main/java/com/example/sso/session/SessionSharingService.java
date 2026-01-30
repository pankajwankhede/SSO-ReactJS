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

/*
 ============================================================================
  Scenario: Multi-tab, multi-realm session sharing (PCC → BCC/RCC)

  1) User logs in on PCC tab
     --------------------------------
     - Browser receives ONE JSESSIONID cookie
     - HttpSession is created (or reused)
     - Backend stores:
         CHANNEL_USER_MAP["PCC"] = ChannelUserDetails(userA)

  2) User opens a new tab for BCC
     --------------------------------
     Request:
       GET /ssoauthenticate?real=BCC&clientID=...

     - BCC realm is NOT present in CHANNEL_USER_MAP
     - LOGOUT_BLOCK_MAP["BCC"] is NOT set
     - sessionSharing rules:
         sso.sessionSharing.rules.BCC.allowFrom = [PCC, RCC]

     - Backend finds PCC session already exists
     - Auto-login happens via sharing:
         CHANNEL_USER_MAP["BCC"] = ChannelUserDetails(userA)
         attributes:
           autoLogin = true
           sharedFrom = "PCC"

     - User is NOT asked for credentials
     - Authorization code is generated and returned

  3) User logs out from BCC tab (Option B behavior)
     --------------------------------
     - Backend removes:
         CHANNEL_USER_MAP["BCC"]
     - Backend sets:
         LOGOUT_BLOCK_MAP["BCC"] = true

     - PCC session REMAINS ACTIVE:
         CHANNEL_USER_MAP["PCC"] is untouched

  4) User opens BCC again while PCC is still logged in
     --------------------------------
     - Backend sees:
         LOGOUT_BLOCK_MAP["BCC"] = true
     - Auto-login via sharing is BLOCKED
     - Login screen is shown for BCC
     - User must manually authenticate for BCC again

  5) Session cleanup rule
     --------------------------------
     - If a logout removes the LAST realm session
       (CHANNEL_USER_MAP becomes empty)
     - HttpSession is invalidated to clean up
 ============================================================================
*/


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
