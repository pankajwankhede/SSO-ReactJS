package com.example.sso.session;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LogoutBlockSessionStore {

  @SuppressWarnings("unchecked")
  private Map<String, Boolean> map(HttpSession session) {
    Object existing = session.getAttribute(SessionKeys.LOGOUT_BLOCK_MAP);
    if (existing instanceof Map<?, ?> m) return (Map<String, Boolean>) m;

    Map<String, Boolean> newMap = new ConcurrentHashMap<>();
    session.setAttribute(SessionKeys.LOGOUT_BLOCK_MAP, newMap);
    return newMap;
  }

  public boolean isBlocked(HttpSession session, String realm) {
    return Boolean.TRUE.equals(map(session).get(realm.toUpperCase()));
  }

  public void block(HttpSession session, String realm) {
    map(session).put(realm.toUpperCase(), Boolean.TRUE);
  }

  public void unblock(HttpSession session, String realm) {
    map(session).remove(realm.toUpperCase());
  }
}
