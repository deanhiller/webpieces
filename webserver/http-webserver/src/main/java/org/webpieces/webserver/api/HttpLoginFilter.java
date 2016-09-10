package org.webpieces.webserver.api;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.ctx.api.Session;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.router.api.routing.RouteFilter;
import org.webpieces.router.api.routing.RouteId;
import org.webpieces.util.filters.Service;

@Singleton
public class HttpLoginFilter implements RouteFilter<LoginInfo> {

	private String token;
	private RouteId loginRoute;

	@Inject
	public HttpLoginFilter() {
	}

	@Override
	public void initialize(LoginInfo initialConfig) {
		token = initialConfig.getTokenThatExistsIfLoggedIn();
		loginRoute = initialConfig.getLoginRouteId();
	}
	
	@Override
	public CompletableFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> next) {
		Session session = Current.session();
		if(session.containsKey(token))
			return next.invoke(meta);

		RouterRequest request = Current.request();
		if(request.method == HttpMethod.GET) {
			//store url requested in flash so after logging in, we can redirect the user
			//back to the original page
			Current.flash().put("url", Current.request().relativePath);
		}
		
		//redirect to login page..
		return CompletableFuture.completedFuture(Actions.redirect(loginRoute));
	}

}
