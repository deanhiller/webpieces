package org.webpieces.router.api;

import java.util.concurrent.CompletableFuture;

public interface HttpFilter {

	//hmmmmmmm, how to do this one as some may need to return way more?
	//CompletableFuture<> filter(Object req, HttpFilter nextFilter);
	
}
