package com.example.sso.web;

import com.example.sso.oauth.OAuthReqSessionStore;
import com.example.sso.session.ChannelUserSessionStore;
import com.example.sso.session.LogoutBlockSessionStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
public class RealmLogoutController {

  private final ChannelUserSessionStore channelUserStore;
  private final OAuthReqSessionStore oauthReqStore;
  private final LogoutBlockSessionStore logoutBlockStore;

  public RealmLogoutController(ChannelUserSessionStore channelUserStore,
                               OAuthReqSessionStore oauthReqStore,
                               LogoutBlockSessionStore logoutBlockStore) {
    this.channelUserStore = channelUserStore;
    this.oauthReqStore = oauthReqStore;
    this.logoutBlockStore = logoutBlockStore;
  }

  public record LogoutRequest(String realm) {}
  public record LogoutResponse(String status, String message, String loggedOutRealm) {}

  @PostMapping("/logout")
  public LogoutResponse logout(@RequestBody LogoutRequest body, HttpServletRequest request) {

    HttpSession session = request.getSession(false);
    if (session == null) return new LogoutResponse("OK", "No active session", null);

    if (body.realm() == null || body.realm().isBlank()) {
      return new LogoutResponse("FAIL", "realm is required", null);
    }

    String realm = body.realm().trim().toUpperCase();

    // 1) Remove only realm login
    channelUserStore.remove(session, realm);

    // 2) Option B: block auto-login for this realm until manual login
    logoutBlockStore.block(session, realm);

    // 3) If no other realm sessions AND no pending OAuthReq, invalidate the whole session (cleanup)
    boolean noRealmUsers = channelUserStore.isEmpty(session);
    boolean noPendingAuthReq = oauthReqStore.isEmpty(session);

    if (noRealmUsers && noPendingAuthReq) {
      session.invalidate();
      return new LogoutResponse("OK", "Logged out realm " + realm + " and cleared empty session", realm);
    }

    return new LogoutResponse("OK", "Logged out realm " + realm + " (auto-login blocked)", realm);
  }
}
