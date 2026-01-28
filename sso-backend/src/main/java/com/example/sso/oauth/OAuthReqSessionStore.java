package com.example.sso.oauth;

import com.example.sso.session.SessionKeys;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OAuthReqSessionStore {

  @SuppressWarnings("unchecked")
  private Map<String, OAuthReq> map(HttpSession session) {
    Object existing = session.getAttribute(SessionKeys.AUTHZ_REQ_MAP);
    if (existing instanceof Map<?, ?> m) return (Map<String, OAuthReq>) m;

    Map<String, OAuthReq> newMap = new ConcurrentHashMap<>();
    session.setAttribute(SessionKeys.AUTHZ_REQ_MAP, newMap);
    return newMap;
  }

  public static String key(String realm, String clientId) {
    return realm.toUpperCase() + "::" + clientId;
  }

  public void put(HttpSession session, OAuthReq req) {
    map(session).put(key(req.realm, req.clientId), req);
  }

  public OAuthReq get(HttpSession session, String realm, String clientId) {
    return map(session).get(key(realm, clientId));
  }

  public void remove(HttpSession session, String realm, String clientId) {
    map(session).remove(key(realm, clientId));
  }

  public boolean isEmpty(HttpSession session) {
    return map(session).isEmpty();
  }
}
