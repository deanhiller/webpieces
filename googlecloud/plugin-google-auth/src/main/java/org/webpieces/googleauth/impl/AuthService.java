package org.webpieces.googleauth.impl;

import org.webpieces.googleauth.api.*;
import org.webpieces.googleauth.client.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.Current;
import org.webpieces.http.exception.ForbiddenException;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.router.api.routes.RouteId;
import org.webpieces.router.impl.RoutingHolder;
import org.webpieces.util.futures.XFuture;
import org.webpieces.util.net.URLEncoder;

import javax.inject.Inject;

import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.*;


public class AuthService {
    public static final String AUTH0_SECRET_KEY = "auth0.redirect.secret";
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    public static final int SIZE = 64;

    protected final AuthApi authApi;
    protected final AuthApiConfig authConfig;
    private final GoogleAuth googleAuth;
    protected final GoogleAuthConfig authRouteIdSet;
    protected final RoutingHolder holder;
    protected SaveUser saveUser;
    protected SecureRandom random = new SecureRandom();

    @Inject
    public AuthService(
            AuthApi authApi,
            AuthApiConfig authConfig,
            RoutingHolder holder,
            GoogleAuth googleAuth,
            GoogleAuthConfig authRouteIdSet,
            SaveUser saveUser
    ) {

        this.authApi = authApi;
        this.authConfig = authConfig;
        this.googleAuth = googleAuth;
        this.authRouteIdSet = authRouteIdSet;
        this.saveUser = saveUser;
        this.holder = holder;
    }

    public Redirect logout() {
        //remove logged in token
        Current.session().remove(GoogleAuthPlugin.USER_ID_TOKEN);

        RouteId renderAfterLogout = authRouteIdSet.getToRenderAfterLogout();
        return Actions.redirect(renderAfterLogout);
    }

    public Redirect login() {
        String gcpScopes = authRouteIdSet.getGcpScopes();
        String urlEncodedScope = URLEncoder.encode(gcpScopes, Charset.defaultCharset());
        String plainUrl = authConfig.getCallbackUrl();
        String urlEncodedCallbackUrl = URLEncoder.encode(plainUrl, Charset.defaultCharset());

        String urlEncodedSecret = generateSecret();

        //From google docs https://developers.google.com/identity/protocols/oauth2/web-server#httprest_1
        //From https://www.daimto.com/how-to-get-a-google-access-token-with-curl/
        //
        //https://accounts.google.com/o/oauth2/v2/auth
        // ?client_id=XXXX.apps.googleusercontent.com
        // &redirect_uri=urn:ietf:wg:oauth:2.0:oob
        // &scope=https://www.googleapis.com/auth/userinfo.profile
        // &response_type=code

        String url = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + authConfig.getClientId() +
                "&redirect_uri="+ urlEncodedCallbackUrl +
                "&state="+urlEncodedSecret+
                "&scope=" + urlEncodedScope +
                "&access_type=offline"+
                "&response_type=code";

        Current.flash().keep(true); //we must keep previous data like the url AND the secret as well

        log.info("redirect url="+url);
        return Actions.redirectToUrl(url);
    }

    private String generateSecret() {
        byte[] bytes = new byte[SIZE];
        random.nextBytes(bytes);
        String secret = Base64.getEncoder().encodeToString(bytes);
        String urlEncodedSecret = URLEncoder.encode(secret, Charset.defaultCharset());
        //only session cookie is secure so can't be tampered with
        // (ie. no one can generate and stick a valid key in our cookie)
        Current.session().put(AUTH0_SECRET_KEY, secret);
        log.info("put in session="+secret+" AND auth0="+urlEncodedSecret);
        return urlEncodedSecret;
    }

    public XFuture<Redirect> callback() {
        log.info("queryParams="+Current.request().queryParams);
        Map<String, List<String>> queryParams = Current.request().queryParams;
        String code = fetch(queryParams, "code");
        if(code == null) {
            Current.session().remove(GoogleAuthPlugin.USER_ID_TOKEN);  //remove token in case there too
            Current.flash().keep(true);
            return XFuture.completedFuture(Actions.redirect(authRouteIdSet.getLoginDeclinedRoute()));
        }

        validateToken(queryParams);

        FetchTokenRequest request = new FetchTokenRequest();
        request.setClientId(authConfig.getClientId());
        request.setClientSecret(authConfig.getClientSecret());
        request.setCode(code);
        //must match callbackUrl we used in login but should be unused
        request.setCallbackUrl(authConfig.getCallbackUrl());
        request.setScope(authRouteIdSet.getGcpScopes());
        request.setAccessType("offline"); //users can discard the refresh token

        return authApi.fetchToken(request)
                .thenCompose( (resp) -> validateToken(resp))
                .thenCompose( (resp2) -> fetchPageToRedirectTo(resp2));
    }

    private XFuture<ProfileAndTokens> validateToken(FetchTokenResponse resp) {
        return googleAuth.fetchProfile(resp.getIdToken())
                .thenApply((r) -> new ProfileAndTokens(resp, r));
    }

    private void validateToken(Map<String, List<String>> queryParams) {
        //all queryParams are run through url decoding so no need to decode it...
        String stateDecoded = fetch(queryParams, "state");
        String base64Session = Current.session().remove(AUTH0_SECRET_KEY);
        log.info("fetch from session="+base64Session+"   state from auth0="+stateDecoded);

        //SECURITY, do not remove.  Cookie can't be tampered with or webpieces throws exception
        //so we store secret in cookie and only auth0 can redirect back to this url after we
        //redirect to auth0 ->
        if(!base64Session.equals(stateDecoded))
            throw new ForbiddenException("You cheater!!!  no soup for you! state="+stateDecoded+" session="+base64Session);
    }

    public XFuture<Redirect> fetchPageToRedirectTo(ProfileAndTokens tokensAndProfile) {
        XFuture<Void> saveUserAction = saveUser.saveUserIfNotExist(tokensAndProfile);

        return saveUserAction.thenApply( (resp) -> continueRedirect(tokensAndProfile));
    }

    private Redirect continueRedirect(ProfileAndTokens response) {
        String email = response.getProfile().getEmail();
        if(email == null) {
            throw new IllegalStateException("saveUserIfNotExist returned a null email in SaveUserResponse");
        }
        Current.session().put(GoogleAuthPlugin.USER_ID_TOKEN, email);

        //5 cases of login  (2 and 4 similar and 3 and 5 similar)

        String url = Current.flash().get("url");
        if(url != null) {
            //1. go to secure url, login, and user lands on url he tries to access
            //2. go to secure url, fail login, success login, and user lands on url
            //3. post form data(logged out), login, and user lands on same page with data filled in
            //4. post form data(logged out), fail login, success login, user langs on same page with data filled in
            Set<String> mySet = new HashSet<>(Arrays.asList(authRouteIdSet.getSecureFields()));
            Current.getContext().moveFormParamsToFlash(mySet);
            Current.flash().keep(true);
            return Actions.redirectToUrl(url); //page the user was trying to access before logging in
        }

        //5. (LAST in this page)base login and go to logged in home (easy)

        Current.flash().keep(false);
        Current.validation().keep(false);
        RouteId toRenderAfterLogin = authRouteIdSet.getToRenderAfterLogin();
        return Actions.redirect(toRenderAfterLogin); //base page after login screen
    }

    private String fetch(Map<String, List<String>> queryParams, String token) {
        List<String> strings = queryParams.get(token);
        if(strings == null)
            return null;
        else if(strings.size() == 0)
            return null;
        else if(strings.size() > 1)
            throw new IllegalStateException("Provider returned more than 1 string for tokenkey="+token+" list="+strings);

        return strings.get(0);
    }
}
