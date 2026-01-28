package com.example.sso.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "sso.realms")
public class RealmFeatureProperties {

  private Map<String, RealmFeatures> realms = new HashMap<>();

  public Map<String, RealmFeatures> getRealms() { return realms; }
  public void setRealms(Map<String, RealmFeatures> realms) { this.realms = realms; }

  public RealmFeatures get(String realm) {
    if (realm == null) return new RealmFeatures(true, true);
    return realms.getOrDefault(realm.toUpperCase(), new RealmFeatures(true, true));
  }

  public static class RealmFeatures {
    private boolean forgotUsername = true;
    private boolean forgotPassword = true;

    public RealmFeatures() {}
    public RealmFeatures(boolean forgotUsername, boolean forgotPassword) {
      this.forgotUsername = forgotUsername;
      this.forgotPassword = forgotPassword;
    }

    public boolean isForgotUsername() { return forgotUsername; }
    public void setForgotUsername(boolean forgotUsername) { this.forgotUsername = forgotUsername; }

    public boolean isForgotPassword() { return forgotPassword; }
    public void setForgotPassword(boolean forgotPassword) { this.forgotPassword = forgotPassword; }
  }
}
