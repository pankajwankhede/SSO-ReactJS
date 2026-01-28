package com.example.sso.web;

import com.example.sso.config.RealmFeatureProperties;
import com.example.sso.session.ChannelUserSessionStore;
import com.example.sso.session.LogoutBlockSessionStore;
import com.example.sso.web.dto.ApiResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class LoginInfoController {

  private final RealmFeatureProperties realmFeatures;
  private final ChannelUserSessionStore channelUserStore;
  private final LogoutBlockSessionStore logoutBlockStore;

  public LoginInfoController(RealmFeatureProperties realmFeatures,
                             ChannelUserSessionStore channelUserStore,
                             LogoutBlockSessionStore logoutBlockStore) {
    this.realmFeatures = realmFeatures;
    this.channelUserStore = channelUserStore;
    this.logoutBlockStore = logoutBlockStore;
  }

  // UI calls this to know which buttons to show (forgot username/password)
  @GetMapping("/login")
  public ApiResponse loginInfo(HttpSession session,
                               @RequestParam("real") String realm,
                               @RequestParam(value = "clientID", required = false) String clientId) {

    String r = realm.toUpperCase();
    var cfg = realmFeatures.get(r);

    boolean loggedInForRealm = channelUserStore.get(session, r) != null;
    boolean autoLoginBlocked = logoutBlockStore.isBlocked(session, r);

    return ApiResponse.ok(r, clientId, Map.of(
        "loggedInForRealm", loggedInForRealm,
        "autoLoginBlocked", autoLoginBlocked,
        "enableForgotUsername", cfg.isForgotUsername(),
        "enableForgotPassword", cfg.isForgotPassword()
    ));
  }
}
