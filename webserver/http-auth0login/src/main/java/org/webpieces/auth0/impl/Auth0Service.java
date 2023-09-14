package org.webpieces.auth0.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.auth0.api.*;
import org.webpieces.auth0.client.api.*;
import org.webpieces.ctx.api.Current;
import org.webpieces.http.exception.ForbiddenException;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.router.api.routes.RouteId;
import org.webpieces.router.impl.RoutingHolder;
import org.webpieces.util.SingletonSupplier;
import org.webpieces.util.futures.XFuture;
import org.webpieces.util.net.URLEncoder;

import javax.inject.Inject;

import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.*;


public class Auth0Service {
    public static final String AUTH0_SECRET_KEY = "auth0.redirect.secret";
    private static final Logger log = LoggerFactory.getLogger(Auth0Service.class);
    public static final int SIZE = 64;

    protected final AuthApi authApi;
    protected final JwtDecoder jwtDecoder;
    protected final Auth0ApiConfig auth0Config;
    protected final Auth0Config authRouteIdSet;
    protected final RoutingHolder holder;
    protected SaveUser saveUser;
    protected final SingletonSupplier<String> urlEncodedCallbackUrl;
    protected final SingletonSupplier<String> callbackUrl;
    protected SecureRandom random = new SecureRandom();

    @Inject
    public Auth0Service(
            AuthApi authApi,
            JwtDecoder jwtDecoder,
            Auth0ApiConfig auth0Config,
            RoutingHolder holder,
            Auth0Config authRouteIdSet,
            SaveUser saveUser
    ) {

        this.authApi = authApi;
        this.jwtDecoder = jwtDecoder;
        this.auth0Config = auth0Config;
        this.authRouteIdSet = authRouteIdSet;
        this.saveUser = saveUser;
        this.holder = holder;
        random.setSeed(System.currentTimeMillis());

        this.callbackUrl = new SingletonSupplier<>( () ->
                holder.getReverseRouteLookup().convertToUrl(Auth0RouteId.CALLBACK, true)
        );

        this.urlEncodedCallbackUrl = new SingletonSupplier<>( () -> {
            String url = callbackUrl.get();
            return URLEncoder.encode(url, Charset.defaultCharset());
        });
    }
    public Redirect logout() {
        Current.session().remove(Auth0Plugin.USER_ID_TOKEN);

        RouteId renderAferLogout = authRouteIdSet.getToRenderAfterLogout();
        String loginUrl = holder.getReverseRouteLookup().convertToUrl(renderAferLogout, true);
        String domain = auth0Config.getAuth0Domain();
        String clientId = auth0Config.getClientId();
        String redirectUrl = "https://"+domain+"/v2/logout?client_id="+clientId+"&returnTo="+loginUrl;

        return Actions.redirectToUrl(redirectUrl);
    }

    public Redirect login() {
        byte[] bytes = new byte[SIZE];
        random.nextBytes(bytes);
        String secret = Base64.getEncoder().encodeToString(bytes);
        String urlEncodedSecret = URLEncoder.encode(secret, Charset.defaultCharset());

        String gmailScopes = authRouteIdSet.getGmailScopes();
        String urlEncodedScope = URLEncoder.encode(gmailScopes, Charset.defaultCharset());
        String audience = "";
        if(auth0Config.getAudience() != null) {
            String urlEncodedAudience = URLEncoder.encode(auth0Config.getAudience(), Charset.defaultCharset());
            audience = "&audience="+urlEncodedAudience;
        }

        //only session cookie is secure so can't be tampered with
        // (ie. no one can generate and stick a valid key in our cookie)
        Current.session().put(AUTH0_SECRET_KEY, secret);
        log.info("put in session="+secret+" AND auth0="+urlEncodedSecret);

        String domain = auth0Config.getAuth0Domain();
        String url = "https://"+domain+"/authorize" +
                "?response_type=code" +
                "&client_id=" + auth0Config.getClientId() +
                "&scope=" + urlEncodedScope +
                "&state="+urlEncodedSecret+
                audience +
                "&redirect_uri="+ urlEncodedCallbackUrl.get();

        Current.flash().keep(true); //we must keep previous data like the url AND the secret as well

        log.info("redirect url="+url);
        return Actions.redirectToUrl(url);
    }

    public XFuture<Redirect> callback() {
        log.info("queryParams="+Current.request().queryParams);
        Map<String, List<String>> queryParams = Current.request().queryParams;
        String code = fetch(queryParams, "code");
        if(code == null) {
            Current.session().remove(Auth0Plugin.USER_ID_TOKEN);  //remove token in case there too
            Current.flash().keep(true);
            return XFuture.completedFuture(Actions.redirect(authRouteIdSet.getLoginDeclinedRoute()));
        }

        validateToken(queryParams);

        FetchTokenRequest request = new FetchTokenRequest();
        request.setClientId(auth0Config.getClientId());
        request.setClientSecret(auth0Config.getClientSecret());
        request.setCode(code);
        //must match callbackUrl we used in login but should be unused
        request.setCallbackUrl(callbackUrl.get());
        request.setAudience(auth0Config.getAudience());
        request.setScope(authRouteIdSet.getGmailScopes());

        return authApi.fetchToken(request)
                .thenApply((resp) -> jwtDecoder.decodeJwt(resp))
                .thenCompose( (profile) -> fetchPageToRedirectTo(profile));
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

    public XFuture<Redirect> fetchPageToRedirectTo(JwtAuth0Body profile) {
        if(profile.getEmail() == null)
            throw new IllegalStateException("No email so cannot lookup user");
        else if(profile.getEmail().trim().equals(""))
            throw new IllegalStateException("Email from google is all whitespace and no email.  we cannot proceed");

        XFuture<Void> saveUserAction = saveUser.saveUserIfNotExist(profile);

        return saveUserAction.thenApply( (v) -> continueRedirect(profile));
    }

    private Redirect continueRedirect(JwtAuth0Body profile) {
        Current.session().put(Auth0Plugin.USER_ID_TOKEN, profile.getEmail());

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
