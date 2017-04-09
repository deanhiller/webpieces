package org.webpieces.webserver.api.login;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.ctx.api.Session;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.router.api.routing.RouteFilter;
import org.webpieces.router.api.routing.RouteId;
import org.webpieces.util.filters.Service;

public class LoginFilter extends RouteFilter<LoginInfo> {

	private String token;
	private RouteId loginRoute;

	@Inject
	public LoginFilter() {
	}

	@Override
	public void initialize(LoginInfo initialConfig) {
		token = initialConfig.getTokenThatExistsIfLoggedIn();
		loginRoute = initialConfig.getLoginRouteId();
	}
	
	@Override
	public CompletableFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> next) {
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
				
			Current.flash().keep();
		}
		
		//redirect to login page..
		return CompletableFuture.completedFuture(Actions.redirect(loginRoute));
	}

	private Object addCacheHeaders(Object response) {
		HttpResponse resp = (HttpResponse) response;
		//http://stackoverflow.com/questions/49547/how-to-control-web-page-caching-across-all-browsers
		//This forces the browser back button to re-request the page as it would never have the page
		//and is good to use to hide banking information type pages
		//resp.addHeader(new Header(KnownHeaderName.CACHE_CONTROL, "no-store")); 
		resp.addHeader(new Header(KnownHeaderName.CACHE_CONTROL, "no-cache, no-store, must-revalidate"));
		resp.addHeader(new Header(KnownHeaderName.PRAGMA, "no-cache"));
		resp.addHeader(new Header(KnownHeaderName.EXPIRES, "0"));
		return resp;
	}

}
