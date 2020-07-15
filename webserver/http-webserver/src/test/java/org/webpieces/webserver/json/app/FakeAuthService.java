package org.webpieces.webserver.json.app;

import java.util.concurrent.CompletableFuture;

public class FakeAuthService {

	public CompletableFuture<Boolean> authenticate(String username) {
		return CompletableFuture.completedFuture(true);
	}

	public void saveRequest(SearchRequest request) {
	}
}
