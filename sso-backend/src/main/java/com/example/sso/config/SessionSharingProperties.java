package com.example.sso.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.*;

@ConfigurationProperties(prefix = "sso.sessionSharing")
public class SessionSharingProperties {

  /**
   * Key   : Target realm (BCC / PCC / RCC / IHH)
   * Value : Sharing rules for that realm
   */
  private Map<String, Rule> rules = new HashMap<>();

  public Map<String, Rule> getRules() {
    return rules;
  }

  public void setRules(Map<String, Rule> rules) {
    this.rules = rules;
  }

  /**
   * Get rule for a given realm safely
   */
  public Rule ruleFor(String realm) {
    if (realm == null) return new Rule();
    return rules.getOrDefault(realm.toUpperCase(), new Rule());
  }

  // ===============================
  // Inner Rule class
  // ===============================
  public static class Rule {

    /**
     * List of source realms allowed to share session
     * Example: [PCC, RCC]
     */
    private List<String> allowFrom = new ArrayList<>();

    public List<String> getAllowFrom() {
      return allowFrom;
    }

    public void setAllowFrom(List<String> allowFrom) {
      this.allowFrom = allowFrom;
    }

    /**
     * Normalized (UPPERCASE) set for runtime checks
     */
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
