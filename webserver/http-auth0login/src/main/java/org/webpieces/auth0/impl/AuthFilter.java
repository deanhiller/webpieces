package org.webpieces.auth0.impl;

import com.webpieces.http2.api.dto.highlevel.Http2Headers;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import org.webpieces.auth0.api.AbstractAuthController;
import org.webpieces.auth0.api.AuthRouteIdSet;
import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.ctx.api.Session;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.router.api.routes.RouteId;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.FutureHelper;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AuthFilter extends RouteFilter<AuthRouteIdSet> {

	private FutureHelper futureUtil;

	private RouteId loginRoute;
	private String[] secureFields;

	@Inject
	public AuthFilter(FutureHelper futureUtil) {
		this.futureUtil = futureUtil;
	}

	@Override
	public void initialize(AuthRouteIdSet initialConfig) {
		loginRoute = initialConfig.getLoginRoute();
		secureFields = initialConfig.getSecureFields();
	}
	
	@Override
	public XFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> next) {
		Session session = Current.session();
		if(session.containsKey(AbstractAuthController.USER_ID_TOKEN)) {
			Current.addModifyResponse(resp -> addCacheHeaders(resp));
			
			return futureUtil.finallyBlock(
					() -> next.invoke(meta),
					() -> clearSecureFields(meta));
		}

		RouterRequest request = Current.request();
		if(request.isAjaxRequest) {
			if(request.referrer != null) {
				Current.flash().put("url", request.referrer);
				Current.flash().keep(true);
			}
			
			return XFuture.completedFuture(Actions.ajaxRedirect(loginRoute));	
		} else if(request.method == HttpMethod.GET) {
			//store url requested in flash so after logging in, we can redirect the user
			//back to the original page
			Current.flash().put("url", request.relativePath);
			Current.flash().keep(true);
		} else if (request.method == HttpMethod.POST) {
			//adding a validation error avoids the posting of the form so they post AFTER logging in
			if(request.referrer != null)
				Current.flash().put("url", request.referrer);
			else
				Current.flash().put("url", request.relativePath);
				
			Set<String> mySet = new HashSet<>(Arrays.asList(secureFields));
			Current.getContext().moveFormParamsToFlash(mySet);

			Current.flash().keep(true);
		}
		
		//redirect to login page..
		return XFuture.completedFuture(Actions.redirect(loginRoute));
	}

	private void clearSecureFields(MethodMeta meta) {
		//clear out secure fields
		for(String secureKey: secureFields) {
			meta.getCtx().getFlash().remove(secureKey);
		}
		return;
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
