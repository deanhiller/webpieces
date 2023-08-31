package org.webpieces.auth0.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.auth0.client.api.*;
import org.webpieces.auth0.impl.Auth0ApiConfig;
import org.webpieces.ctx.api.Current;
import org.webpieces.http.exception.ForbiddenException;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.router.api.plugins.ReverseRouteLookup;
import org.webpieces.router.api.routes.RouteId;
import org.webpieces.router.impl.RoutingHolder;
import org.webpieces.util.SingletonSupplier;
import org.webpieces.util.futures.XFuture;
import org.webpieces.util.net.URLEncoder;

import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.*;

public abstract class AbstractAuthController {

	private static final Logger log = LoggerFactory.getLogger(AbstractAuthController.class);

	public static final String USER_ID_TOKEN = "userId";
	private static final int SIZE = 64;
	private static final String AUTH0_SECRET_KEY = "auth0.redirect.secret";
	private final ReverseRouteLookup reverseRouteLookup;
	private Auth0Config authRouteIdSet;
	private final AuthApi authApi;
	private final SingletonSupplier<String> urlEncodedCallbackUrl;
	private final SingletonSupplier<String> callbackUrl;
	private Auth0ApiConfig auth0Config;

	private SecureRandom random = new SecureRandom();

	public AbstractAuthController(
			AuthApi authAPI,
			Auth0ApiConfig auth0Config,
			RoutingHolder holder,
			Auth0Config authRouteIdSet
	) {
		this.authApi = authAPI;
		this.auth0Config = auth0Config;
		this.reverseRouteLookup = holder.getReverseRouteLookup();
		this.authRouteIdSet = authRouteIdSet;
		random.setSeed(System.currentTimeMillis());

		RouteId callbackRoute = authRouteIdSet.getCallbackRoute();
		this.callbackUrl = new SingletonSupplier<>( () -> reverseRouteLookup.convertToUrl(callbackRoute, true));
		this.urlEncodedCallbackUrl = new SingletonSupplier<>( () -> {
			String url = callbackUrl.get();
			return URLEncoder.encode(url, Charset.defaultCharset());
		});
	}

	public Action logout() {
		Current.session().remove(USER_ID_TOKEN);

		RouteId renderAferLogout = authRouteIdSet.getToRenderAfterLogout();
		String loginUrl = reverseRouteLookup.convertToUrl(renderAferLogout, true);
		String domain = auth0Config.getAuth0Domain();
		String clientId = auth0Config.getClientId();
		String redirectUrl = "https://"+domain+"/v2/logout?client_id="+clientId+"&returnTo="+loginUrl;

		return Actions.redirectToUrl(redirectUrl);
	}
	
	public Action login() throws Exception {
		byte[] bytes = new byte[SIZE];
		random.nextBytes(bytes);
		String secret = Base64.getEncoder().encodeToString(bytes);
		String urlEncodedSecret = URLEncoder.encode(secret, Charset.defaultCharset());
		//WHAT!! they fail if we url encode the scope (they have bugs!!!)
		String urlEncodedScope = URLEncoder.encode("openid profile email phone", Charset.defaultCharset());

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
				"&redirect_uri="+ urlEncodedCallbackUrl.get();

		Current.flash().keep(true); //we must keep previous data like the url AND the secret as well

		log.info("redirect url="+url);
		return Actions.redirectToUrl(url);
	}

	public XFuture<Action> callback() throws Exception {
		log.info("queryParams="+Current.request().queryParams);
		Map<String, List<String>> queryParams = Current.request().queryParams;
		String code = fetch(queryParams, "code");
		if(code == null) {
			Current.session().remove(AbstractAuthController.USER_ID_TOKEN);  //remove token in case there too
			Current.flash().keep(true);
			return XFuture.completedFuture(Actions.redirect(authRouteIdSet.getLoginDeclinedRoute()));
		}

		validateToken(queryParams);

		AuthRequest request = new AuthRequest();
		request.setClientId(auth0Config.getClientId());
		request.setClientSecret(auth0Config.getClientSecret());
		request.setCode(code);
		//must match callbackUrl we used in login but should be unused
		request.setCallbackUrl(callbackUrl.get());

		return authApi.codeToTokens(request)
				.thenCompose((resp) -> fetchProfile(resp))
				.thenApply( (profile) -> fetchPageToRedirectTo(profile));
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

	private XFuture<UserProfile> fetchProfile(AuthResponse resp) {
		FetchProfileRequest fetchProfile = new FetchProfileRequest();
		fetchProfile.setAccessToken(resp.getAccessToken());
		return authApi.fetchProfile(fetchProfile);
	}

	public Redirect fetchPageToRedirectTo(UserProfile profile) {

		if(profile.getEmail() == null)
			throw new IllegalStateException("Email was null from google, we cannot proceed");
		else if(profile.getEmail().trim().equals(""))
			throw new IllegalStateException("Email from google is all whitespace and no email.  we cannot proceed");

		saveUserIfNotExist(profile);

		Current.session().put(USER_ID_TOKEN, profile.getEmail());

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

	/**
	 * @param profile the profile of the user
	 * @return true if user exists and false if user does not exist
	 */
	protected abstract boolean saveUserIfNotExist(UserProfile profile);
	
}
