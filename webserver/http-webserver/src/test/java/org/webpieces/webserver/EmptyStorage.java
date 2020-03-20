package org.webpieces.webserver;

import org.webpieces.router.api.extensions.SimpleStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class EmptyStorage implements SimpleStorage {

	@Override
	public CompletableFuture<Void> save(String key, String subKey, String value) {
		
		return null;
	}

	@Override
	public CompletableFuture<Void> save(String key, Map<String, String> properties) {
		
		return null;
	}

	@Override
	public CompletableFuture<Map<String, String>> read(String key) {
		return CompletableFuture.completedFuture(new HashMap<>());
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
