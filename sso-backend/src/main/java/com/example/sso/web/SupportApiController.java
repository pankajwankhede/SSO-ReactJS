package com.example.sso.web;

import com.example.sso.config.RealmFeatureProperties;
import com.example.sso.session.ChannelUserDetails;
import com.example.sso.session.ChannelUserSessionStore;
import com.example.sso.web.dto.ApiResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
public class SupportApiController {

  private final RealmFeatureProperties realmFeatures;
  private final ChannelUserSessionStore channelUserStore;

  public SupportApiController(RealmFeatureProperties realmFeatures,
                              ChannelUserSessionStore channelUserStore) {
    this.realmFeatures = realmFeatures;
    this.channelUserStore = channelUserStore;
  }

  public record ForgetUserRequest(String realm, String emailOrPhone) {}
  public record ForgotPasswordRequest(String realm, String usernameOrEmail) {}
  public record PasswordUpdateRequest(String realm, String currentPassword, String newPassword) {}
  public record UserDetailsUpdateRequest(String realm, Map<String, Object> details) {}

  @PostMapping("/ForgetUser")
  public ApiResponse forgetUser(@RequestBody ForgetUserRequest body) {
    String realm = body.realm() == null ? null : body.realm().toUpperCase();
    var cfg = realmFeatures.get(realm);

    if (!cfg.isForgotUsername()) {
      return ApiResponse.fail(realm, null, "Forgot Username is not enabled for this channel");
    }

    // TODO: call downstream service per realm
    return ApiResponse.ok(realm, null, Map.of(
        "flowId", UUID.randomUUID().toString(),
        "message", "ForgetUser flow started (dummy)"
    ));
  }

  @PostMapping("/forgetPassowd")
  public ApiResponse forgotPassword(@RequestBody ForgotPasswordRequest body) {
    String realm = body.realm() == null ? null : body.realm().toUpperCase();
    var cfg = realmFeatures.get(realm);

    if (!cfg.isForgotPassword()) {
      return ApiResponse.fail(realm, null, "Forgot Password is not enabled for this channel");
    }

    // TODO: call downstream service per realm
    return ApiResponse.ok(realm, null, Map.of(
        "flowId", UUID.randomUUID().toString(),
        "message", "ForgotPassword flow started (dummy)"
    ));
  }

  @PostMapping("/UserPasswordUpdate")
  public ApiResponse passwordUpdate(@RequestBody PasswordUpdateRequest body, HttpSession session) {
    String realm = body.realm() == null ? null : body.realm().toUpperCase();

    ChannelUserDetails user = channelUserStore.get(session, realm);
    if (user == null) {
      return ApiResponse.fail(realm, null, "Not authenticated for realm " + realm);
    }

    // TODO: call downstream service per realm
    return ApiResponse.ok(realm, null, Map.of("message", "Password updated (dummy)", "userId", user.getUserId()));
  }

  @PostMapping("/UserDeatislUpdate")
  public ApiResponse detailsUpdate(@RequestBody UserDetailsUpdateRequest body, HttpSession session) {
    String realm = body.realm() == null ? null : body.realm().toUpperCase();

    ChannelUserDetails user = channelUserStore.get(session, realm);
    if (user == null) {
      return ApiResponse.fail(realm, null, "Not authenticated for realm " + realm);
    }

    // TODO: call downstream service per realm
    return ApiResponse.ok(realm, null, Map.of("message", "User details updated (dummy)", "userId", user.getUserId()));
  }
}
