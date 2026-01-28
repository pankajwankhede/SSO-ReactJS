package com.example.sso.web;

import com.example.sso.config.SessionSharingProperties;
import com.example.sso.oauth.OAuthReq;
import com.example.sso.oauth.OAuthReqSessionStore;
import com.example.sso.service.AuthorizationCodeService;
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
import java.util.Map;

@RestController
public class SsoAuthApiController {

  private final OAuthReqSessionStore oauthReqStore;
  private final ChannelUserSessionStore channelUserStore;
  private final AuthorizationCodeService codeService;
  private final SessionSharingProperties sharingProps;
  private final LogoutBlockSessionStore logoutBlockStore;

  public SsoAuthApiController(OAuthReqSessionStore oauthReqStore,
                              ChannelUserSessionStore channelUserStore,
                              AuthorizationCodeService codeService,
                              SessionSharingProperties sharingProps,
                              LogoutBlockSessionStore logoutBlockStore) {
    this.oauthReqStore = oauthReqStore;
    this.channelUserStore = channelUserStore;
    this.codeService = codeService;
    this.sharingProps = sharingProps;
    this.logoutBlockStore = logoutBlockStore;
  }

  @GetMapping("/ssoauthenticate")
  public ApiResponse ssoauthenticate(HttpServletRequest request,
                                    @RequestParam("clientID") String clientId,
                                    @RequestParam("redirectURL") String redirectUrl,
                                    @RequestParam(value = "responseType", required = false) String responseType,
                                    @RequestParam(value = "scope", required = false) String scope,
                                    @RequestParam("state") String state,
                                    @RequestParam("real") String realm) {

    HttpSession session = request.getSession(true);

    // Save OAuth request in session: AUTHZ_REQ_MAP[realm::clientId]
    OAuthReq req = new OAuthReq(clientId, redirectUrl, responseType, scope, state, realm);
    oauthReqStore.put(session, req);

    // 1) already logged in for this realm?
    ChannelUserDetails user = channelUserStore.get(session, realm);

    // 2) if not logged in, try session-sharing unless logout-block is set (Option B)
    if (user == null) {
      if (logoutBlockStore.isBlocked(session, realm)) {
        return ApiResponse.loginRequired(realm.toUpperCase(), clientId);
      }
      user = resolveOrShareRealmSession(session, realm);
    }

    if (user == null) {
      return ApiResponse.loginRequired(realm.toUpperCase(), clientId);
    }

    // Generate code + cleanup OAuthReq only (keep realm session)
    String code = codeService.issueCode(req, user);
    oauthReqStore.remove(session, realm, clientId);

    String redirectTo = redirectUrl + "?code=" + enc(code) + "&state=" + enc(state);
    return ApiResponse.redirect(realm.toUpperCase(), clientId, redirectTo);
  }

  private ChannelUserDetails resolveOrShareRealmSession(HttpSession session, String targetRealm) {
    String realm = targetRealm.toUpperCase();

    ChannelUserDetails existing = channelUserStore.get(session, realm);
    if (existing != null) return existing;

    var rule = sharingProps.ruleFor(realm);
    for (String sourceRealm : rule.allowFromUpper()) {
      ChannelUserDetails src = channelUserStore.get(session, sourceRealm);
      if (src != null) {
        ChannelUserDetails auto = new ChannelUserDetails(
            realm,
            src.getUserId(),
            src.getUsername(),
            src.getRoles(),
            Map.of("autoLogin", true, "sharedFrom", sourceRealm),
            Instant.now()
        );
        channelUserStore.put(session, realm, auto);
        return auto;
      }
    }
    return null;
  }

  private static String enc(String s) {
    return URLEncoder.encode(s, StandardCharsets.UTF_8);
  }
}
