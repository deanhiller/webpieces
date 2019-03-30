package org.webpieces.webserver.api.login;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.ctx.api.Session;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.router.api.routes.RouteId;
import org.webpieces.router.impl.dto.MethodMeta;
import org.webpieces.util.filters.Service;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

public class LoginFilter extends RouteFilter<LoginInfo> {

	private String token;
	private RouteId loginRoute;
	private Pattern patternToMatch;
	private String[] secureFields;

	@Inject
	public LoginFilter() {
	}

	@Override
	public void initialize(LoginInfo initialConfig) {
		token = initialConfig.getTokenThatExistsIfLoggedIn();
		loginRoute = initialConfig.getLoginRouteId();
		secureFields = initialConfig.getSecureFields();
		String securePath = initialConfig.getSecurePath();
		if(securePath != null)
			patternToMatch = Pattern.compile(initialConfig.getSecurePath());
	}
	
	@Override
	public CompletableFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> next) {
		if(patternToMatch != null) {
			//If we have a securePath, we act as a NotFoundFilter so we want to redirect to Login ONLY if this is a secure path request
			//This hides known vs. unknown pages
			Matcher matcher = patternToMatch.matcher(meta.getCtx().getRequest().relativePath);
			if(!matcher.matches())
				return next.invoke(meta);
		}
	
		Session session = Current.session();
		if(session.containsKey(token)) {
			Current.addModifyResponse(resp -> addCacheHeaders(resp));
			return next.invoke(meta);
		}

		RouterRequest request = Current.request();
		if(request.isAjaxRequest) {
			if(request.referrer != null) {
				Current.flash().put("url", request.referrer);
				Current.flash().keep();
			}
			
			return CompletableFuture.completedFuture(Actions.ajaxRedirect(loginRoute));	
		} else if(request.method == HttpMethod.GET) {
			//store url requested in flash so after logging in, we can redirect the user
			//back to the original page
			Current.flash().put("url", request.relativePath);
			Current.flash().keep();
		} else if (request.method == HttpMethod.POST) {
			//adding a validation error avoids the posting of the form so they post AFTER logging in
			if(request.referrer != null)
				Current.flash().put("url", request.referrer);
			else
				Current.flash().put("url", request.relativePath);
				
			Set<String> mySet = new HashSet<>(Arrays.asList(secureFields));
			Current.getContext().moveFormParamsToFlash(mySet);
			
			Current.flash().keep();
		}
		
		//redirect to login page..
		return CompletableFuture.completedFuture(Actions.redirect(loginRoute));
	}

	private Object addCacheHeaders(Object response) {
		Http2Headers resp = (Http2Headers) response;
		//http://stackoverflow.com/questions/49547/how-to-control-web-page-caching-across-all-browsers
		//This forces the browser back button to re-request the page as it would never have the page
		//and is good to use to hide banking information type pages
		//resp.addHeader(new Header(KnownHeaderName.CACHE_CONTROL, "no-store")); 
		resp.addHeader(new Http2Header(Http2HeaderName.CACHE_CONTROL, "no-cache, no-store, must-revalidate"));
		resp.addHeader(new Http2Header(Http2HeaderName.PRAGMA, "no-cache"));
		resp.addHeader(new Http2Header(Http2HeaderName.EXPIRES, "0"));
		return resp;
	}

}
