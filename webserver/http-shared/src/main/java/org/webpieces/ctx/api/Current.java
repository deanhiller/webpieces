package org.webpieces.ctx.api;

public class Current {

	private static ThreadLocal<RequestContext> requestContext = new ThreadLocal<>();

	public static void setContext(RequestContext requestCtx) {
		requestContext.set(requestCtx);
	}
	public static boolean isContextSet() {
		return requestContext.get() != null;
	}
	
	public static RequestContext getContext() {
		RequestContext ctx = requestContext.get();
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
	public static void addModifyResponse(OverwritePlatformResponse callback) {
		getContext().addModifyResponse(callback);
	}
}
