package com.example.sso.session;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChannelUserSessionStore {

  @SuppressWarnings("unchecked")
  private Map<String, ChannelUserDetails> map(HttpSession session) {
    Object existing = session.getAttribute(SessionKeys.CHANNEL_USER_MAP);
    if (existing instanceof Map<?, ?> m) return (Map<String, ChannelUserDetails>) m;

    Map<String, ChannelUserDetails> newMap = new ConcurrentHashMap<>();
    session.setAttribute(SessionKeys.CHANNEL_USER_MAP, newMap);
    return newMap;
  }

  public ChannelUserDetails get(HttpSession session, String realm) {
    return map(session).get(realm.toUpperCase());
  }

  public void put(HttpSession session, String realm, ChannelUserDetails details) {
    map(session).put(realm.toUpperCase(), details);
  }

  public boolean remove(HttpSession session, String realm) {
    return map(session).remove(realm.toUpperCase()) != null;
  }

  public boolean isEmpty(HttpSession session) {
    return map(session).isEmpty();
  }
}
