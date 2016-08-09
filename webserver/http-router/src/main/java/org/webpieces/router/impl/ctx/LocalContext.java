package org.webpieces.router.impl.ctx;

public class LocalContext {

	private static ThreadLocal<ResponseProcessor> processorLocal = new ThreadLocal<>();
	
	public static ResponseProcessor getResponseProcessor() {
		return processorLocal.get();
	}

	public static void setResponseProcessor(ResponseProcessor processor) {
		processorLocal.set(processor);
	}

}
