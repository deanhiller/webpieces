package org.webpieces.ctx.api;

import org.webpieces.util.context.Context;
import org.webpieces.util.context.WebpiecesContextKey;
import org.webpieces.util.context.PlatformHeaders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Current {

	private static List<PlatformHeaders> platformHeaders = new ArrayList<>();

	/**
	 * Trick to force users to be an enum that extends PlatformHeaders so they don't screw it
	 * up and create a class for some reason
	 */
	public static <T extends Enum & PlatformHeaders> void setupPlatform(List<T> headers) {
		if(headers.size() == 0)
			throw new IllegalArgumentException("Must have at least one platform header");

		platformHeaders.clear();

		for(T header : headers) {
			PlatformHeaders h = header;
			platformHeaders.add(h);
		}
	}

	public static void setContext(RequestContext requestCtx) {

		if(requestCtx == null) {
			Context.clear();
			return;
		}

		Map<String, String> headerMap = translateToSingleHeaders(requestCtx);
		Context.put(Context.HEADERS, headerMap);
		Context.put(Context.REQUEST, requestCtx);
		Context.put(WebpiecesContextKey.REQUEST_PATH.toString(), requestCtx.getRequest().relativePath);

	}

	private static Map<String, String> translateToSingleHeaders(RequestContext requestCtx) {
		Map<String, String> headerMap = new HashMap<>();
		Map<String, List<RouterHeader>> requestHeaders = requestCtx.getRequest().getHeaders();
		for(PlatformHeaders header : platformHeaders) {
			List<RouterHeader> routerHeaders = requestHeaders.get(header.getHeaderName());
			if(routerHeaders == null || routerHeaders.size() == 0)
				continue;
			else if(routerHeaders.size() > 1)
				throw new IllegalStateException("Platform headers does not support the same header multiple times");

			RouterHeader routerHeader = routerHeaders.get(0);
			headerMap.put(routerHeader.getName(), routerHeader.getValue());
		}
		return headerMap;
	}

	public static boolean isContextSet() {
		return Context.get(Context.REQUEST) != null;
	}
	
	public static RequestContext getContext() {
		RequestContext ctx = (RequestContext) Context.get(Context.REQUEST);
		if(ctx == null)
			throw new IllegalArgumentException("This can only be called on the thread that calls "
					+ "the controller method.  (You can cache it though to use later on another thread, so call"
					+ " this on the controller thread and save it for later use)");		
		return ctx;
	}

	public static RouterRequest request() {
		return getContext().getRequest();
	}
	public static Flash flash() {
		return getContext().getFlash();
	}	
	public static Validation validation() {
		return getContext().getValidation();
	}
	public static Session session() {
		return getContext().getSession();
	}
	public static Messages messages() {
		return getContext().getMessages();
	}
	public static ApplicationContext applicationContext() {
		return getContext().getApplicationContext();
	}
	
	public static void addModifyResponse(OverwritePlatformResponse callback) {
		getContext().addModifyResponse(callback);
	}
}
