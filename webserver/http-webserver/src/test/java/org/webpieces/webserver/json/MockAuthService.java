package org.webpieces.webserver.json;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.webserver.json.app.FakeAuthService;
import org.webpieces.webserver.json.app.SearchRequest;

public class MockAuthService extends FakeAuthService {

	private List<CompletableFuture<Boolean>> futures = new ArrayList<CompletableFuture<Boolean>>();
	private SearchRequest request;

	@Override
	public CompletableFuture<Boolean> authenticate(String username) {
		return futures.remove(0);
	}

	public void addValueToReturn(CompletableFuture<Boolean> future) {
		this.futures.add(future);
	}
	
	public void saveRequest(SearchRequest request) {
		this.request = request;
	}
	
	public SearchRequest getCachedRequest() {
		return request;
	}
}
