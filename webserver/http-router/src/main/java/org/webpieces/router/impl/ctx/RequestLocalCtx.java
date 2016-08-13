package org.webpieces.router.impl.ctx;

public class RequestLocalCtx {

	private static ThreadLocal<ResponseProcessor> local = new ThreadLocal<>();

	public static void set(ResponseProcessor processor) {
		local.set(processor);
	}
	
	public static ResponseProcessor get() {
		return local.get();
	}

}
