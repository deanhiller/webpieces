package org.webpieces.util.filters;

import java.util.concurrent.CompletableFuture;

import org.webpieces.util.futures.FutureHelper;

public abstract class Filter<REQ, RESP> {

	//default to this one unless changed out
	private static FutureHelper futureUtil = new FutureHelper();

	public abstract CompletableFuture<RESP> filter(REQ meta, Service<REQ, RESP> nextFilter);
	
	public Filter<REQ, RESP> chain(Filter<REQ, RESP> nextFilter) {
		return new Filter<REQ, RESP>() {
			@Override
			public CompletableFuture<RESP> filter(REQ meta, Service<REQ, RESP> nextFilter) {
				return futureUtil.syncToAsyncException(() -> Filter.this.filter(meta, nextFilter));
			}
		};
	}
	
	public Service<REQ, RESP> chain(Service<REQ, RESP> svc) {
		return new Service<REQ, RESP>() {
			@Override
			public CompletableFuture<RESP> invoke(REQ meta) {
				return futureUtil.syncToAsyncException(() -> Filter.this.filter(meta, svc));
			}
		};
	}
	
	/**
	 * DONE this way so in webpieces, you can override FutureUtil via Guice and it will be replaced everywhere.
	 * ie. if you run into a bug, you can swap him.  OR you can swap him for testing OR you can swap him for
	 * adding metrics OR you can swap him for adding logging.
	 * 
	 * Well, ideally you can swap every piece of webpieces like that ;).
	 * 
	 * @param util
	 */
	public static void setFutureUtil(FutureHelper util) {
		futureUtil = util;
	}
}