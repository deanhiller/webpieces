package org.webpieces.util.filters;

import java.util.concurrent.CompletableFuture;

import org.webpieces.util.futures.ExceptionUtil;

public abstract class Filter<REQ, RESP> {

	public abstract CompletableFuture<RESP> filter(REQ meta, Service<REQ, RESP> nextFilter);

	public Filter<REQ, RESP> chain(Filter<REQ, RESP> nextFilter) {
		return new Filter<REQ, RESP>() {
			@Override
			public CompletableFuture<RESP> filter(REQ meta, Service<REQ, RESP> nextFilter) {
				return ExceptionUtil.wrap(() -> Filter.this.filter(meta, nextFilter));
			}
		};
	}
	
	public Service<REQ, RESP> chain(Service<REQ, RESP> svc) {
		return new Service<REQ, RESP>() {
			@Override
			public CompletableFuture<RESP> invoke(REQ meta) {
				return ExceptionUtil.wrap(() -> Filter.this.filter(meta, svc));
			}
		};
	}
}