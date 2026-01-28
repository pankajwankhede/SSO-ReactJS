package com.example.sso.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.*;

@ConfigurationProperties(prefix = "sso.sessionSharing")
public class SessionSharingProperties {

  private Map<String, Rule> rules = new HashMap<>();

  public Map<String, Rule> getRules() { return rules; }
  public void setRules(Map<String, Rule> rules) { this.rules = rules; }

  public Rule ruleFor(String realm) {
    if (realm == null) return new Rule();
    return rules.getOrDefault(realm.toUpperCase(), new Rule());
  }

  public static class Rule {
    private List<String> allowFrom = new ArrayList<>();
    public List<String> getAllowFrom() { return allowFrom; }
    public void setAllowFrom(List<String> allowFrom) { this.allowFrom = allowFrom; }

    public Set<String> allowFromUpper() {
      Set<String> out = new LinkedHashSet<>();
      for (String r : allowFrom) if (r != null && !r.isBlank()) out.add(r.trim().toUpperCase());
      return out;
    }
  }
}
