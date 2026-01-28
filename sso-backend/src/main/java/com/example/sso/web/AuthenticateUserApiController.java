package com.example.sso.web;

import com.example.sso.oauth.OAuthReq;
import com.example.sso.oauth.OAuthReqSessionStore;
import com.example.sso.service.AuthorizationCodeService;
import com.example.sso.service.LoginServiceClient;
import com.example.sso.session.ChannelUserDetails;
import com.example.sso.session.ChannelUserSessionStore;
import com.example.sso.session.LogoutBlockSessionStore;
import com.example.sso.web.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
public class AuthenticateUserApiController {

  private final OAuthReqSessionStore oauthReqStore;
  private final ChannelUserSessionStore channelUserStore;
  private final LoginServiceClient loginService;
  private final AuthorizationCodeService codeService;
  private final LogoutBlockSessionStore logoutBlockStore;

  public AuthenticateUserApiController(OAuthReqSessionStore oauthReqStore,
                                       ChannelUserSessionStore channelUserStore,
                                       LoginServiceClient loginService,
                                       AuthorizationCodeService codeService,
                                       LogoutBlockSessionStore logoutBlockStore) {
    this.oauthReqStore = oauthReqStore;
    this.channelUserStore = channelUserStore;
    this.loginService = loginService;
    this.codeService = codeService;
    this.logoutBlockStore = logoutBlockStore;
  }

  public record AuthenticateUserRequest(String realm, String clientId, String username, String password) {}

  @PostMapping("/AuthdenticateUser")
  public ApiResponse authenticate(@RequestBody AuthenticateUserRequest body,
                                  HttpServletRequest request) {

    String realm = body.realm() == null ? null : body.realm().toUpperCase();
    String clientId = body.clientId();

    if (realm == null || realm.isBlank() || clientId == null || clientId.isBlank()) {
      return ApiResponse.fail(realm, clientId, "realm and clientId are required");
    }

    HttpSession session = request.getSession(true);

    OAuthReq req = oauthReqStore.get(session, realm, clientId);
    if (req == null) {
      return ApiResponse.fail(realm, clientId, "Authorization request not found. Restart login from channel.");
    }

    var res = loginService.authenticate(realm, clientId, body.username(), body.password());
    if (res == null || !res.success) {
      return ApiResponse.fail(realm, clientId, "Invalid credentials");
    }

    // Protect against session fixation without killing other realms
    request.changeSessionId();

    ChannelUserDetails details = new ChannelUserDetails(
        realm,
        res.userId,
        res.username,
        res.roles != null ? res.roles : List.of(),
        res.attributes != null ? res.attributes : Map.of(),
        Instant.now()
    );
    channelUserStore.put(session, realm, details);

    // Option B: user manually logged in again, unblock auto-login for this realm
    logoutBlockStore.unblock(session, realm);

    String code = codeService.issueCode(req, details);

    // Cleanup OAuthReq only
    oauthReqStore.remove(session, realm, clientId);

    String redirectTo = req.redirectUrl + "?code=" + enc(code) + "&state=" + enc(req.state);
    return ApiResponse.redirect(realm, clientId, redirectTo);
  }

  private static String enc(String s) {
    return URLEncoder.encode(s, StandardCharsets.UTF_8);
  }
}
