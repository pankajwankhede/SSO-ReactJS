package com.example.sso.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.*;

@ConfigurationProperties(prefix = "sso")
public class SsoProperties {

  /**
   * Realm feature flags
   * sso.realms.BCC.forgot-username=true
   */
  private Map<String, RealmFeatures> realms = new HashMap<>();

  /**
   * Session sharing rules
   * sso.sessionSharing.rules.BCC.allowFrom=[PCC,RCC]
   */
  private SessionSharing sessionSharing = new SessionSharing();

  // ===============================
  // Getters / Setters
  // ===============================

  public Map<String, RealmFeatures> getRealms() {
    return realms;
  }

  public void setRealms(Map<String, RealmFeatures> realms) {
    this.realms = realms;
  }

  public SessionSharing getSessionSharing() {
    return sessionSharing;
  }

  public void setSessionSharing(SessionSharing sessionSharing) {
    this.sessionSharing = sessionSharing;
  }

  // ===============================
  // Convenience helpers
  // ===============================

  public RealmFeatures realmFeatures(String realm) {
    if (realm == null) return new RealmFeatures();
    return realms.getOrDefault(realm.toUpperCase(), new RealmFeatures());
  }

  public SessionSharing.Rule sharingRule(String realm) {
    if (realm == null) return new SessionSharing.Rule();
    return sessionSharing.ruleFor(realm);
  }

  // ===============================
  // Inner classes
  // ===============================

  public static class RealmFeatures {
    private boolean forgotUsername = true;
    private boolean forgotPassword = true;

    public boolean isForgotUsername() {
      return forgotUsername;
    }

    public void setForgotUsername(boolean forgotUsername) {
      this.forgotUsername = forgotUsername;
    }

    public boolean isForgotPassword() {
      return forgotPassword;
    }

    public void setForgotPassword(boolean forgotPassword) {
      this.forgotPassword = forgotPassword;
    }
  }

  public static class SessionSharing {

    private Map<String, Rule> rules = new HashMap<>();

    public Map<String, Rule> getRules() {
      return rules;
    }

    public void setRules(Map<String, Rule> rules) {
      this.rules = rules;
    }

    public Rule ruleFor(String realm) {
      if (realm == null) return new Rule();
      return rules.getOrDefault(realm.toUpperCase(), new Rule());
    }

    public static class Rule {
      private List<String> allowFrom = new ArrayList<>();

      public List<String> getAllowFrom() {
        return allowFrom;
      }

      public void setAllowFrom(List<String> allowFrom) {
        this.allowFrom = allowFrom;
      }

      public Set<String> allowFromUpper() {
        Set<String> out = new LinkedHashSet<>();
        for (String r : allowFrom) {
          if (r != null && !r.isBlank()) {
            out.add(r.trim().toUpperCase());
          }
        }
        return out;
      }
    }
  }
}
