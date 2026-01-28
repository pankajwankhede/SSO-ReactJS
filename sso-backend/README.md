# sso-backend (Spring Boot + JSON APIs)

This is a JSON-only SSO backend that supports:
- Multi-tab / multi-realm sessions via Spring Session (Geode)
- AUTHZ request storage: AUTHZ_REQ_MAP[realm::clientId]
- Realm user storage: CHANNEL_USER_MAP[realm]
- Directional session sharing (e.g. BCC can reuse PCC/RCC session)
- Realm-only logout with **auto-login block** (Option B)
- If logout makes session empty, it invalidates the session (cleanup)
- Feature flags per realm (IHH disables forgot flows)

## Endpoints (all JSON)
- GET  /ssoauthenticate
- GET  /login
- POST /AuthdenticateUser
- POST /ForgetUser
- POST /forgetPassowd
- POST /UserPasswordUpdate
- POST /UserDeatislUpdate
- POST /logout

## Run locally (without Geode)
This project includes a `local` profile that uses in-memory sessions.

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

React calls must use `withCredentials: true`.

## Run with Geode
Update `spring.data.geode.pool.default.locators` in `application.yml`, then run:

```bash
./gradlew bootRun
```

## Example calls

### Start flow (from ssologinui)
GET /ssoauthenticate?clientID=pcc-client&redirectURL=https://pcc.company.com/launch&state=abc&real=PCC

### Login
POST /AuthdenticateUser
{
  "realm": "PCC",
  "clientId": "pcc-client",
  "username": "demo",
  "password": "demo"
}

### Logout (realm-only)
POST /logout
{ "realm": "BCC" }
