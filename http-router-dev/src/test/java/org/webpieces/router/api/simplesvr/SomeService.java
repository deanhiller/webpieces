package org.webpieces.router.api.simplesvr;

import java.util.concurrent.CompletableFuture;

public class SomeService {

	public CompletableFuture<Integer> remoteCall() {
		return CompletableFuture.completedFuture(5);
	}

}
