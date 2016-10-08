package org.webpieces.util.filters;

import java.util.concurrent.CompletableFuture;

public interface Filter<REQ, RESP> {

	CompletableFuture<RESP> filter(REQ meta, Service<REQ, RESP> nextFilter);
	
}
