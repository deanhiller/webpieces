package org.webpieces.plugins.sslcert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.router.api.extensions.SimpleStorage;

public class MockSimpleStorage implements SimpleStorage {

	private List<CompletableFuture<Map<String, String>>> readResponses = new ArrayList<>();

	@Override
	public CompletableFuture<Void> save(String key, Map<String, String> properties) {
		
		return null;
	}

	@Override
	public CompletableFuture<Map<String, String>> read(String key) {
		return readResponses.remove(0);
	}

	public void addReadResponse(CompletableFuture<Map<String, String>> future) {
		readResponses.add(future);
	}

	@Override
	public CompletableFuture<Void> save(String key, String subKey, String value) {
		return null;
	}

	@Override
	public CompletableFuture<Void> delete(String key) {
		return null;
	}

	@Override
	public CompletableFuture<Void> delete(String key, String subKey) {
		return null;
	}

}
