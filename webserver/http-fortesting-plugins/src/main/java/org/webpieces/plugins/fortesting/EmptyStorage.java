package org.webpieces.plugins.fortesting;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.router.api.extensions.SimpleStorage;

public class EmptyStorage implements SimpleStorage {

	@Override
	public CompletableFuture<Void> save(String key, String subKey, String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> save(String key, Map<String, String> properties) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Map<String, String>> read(String key) {
		return CompletableFuture.completedFuture(new HashMap<>());
	}

	@Override
	public CompletableFuture<Void> delete(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> delete(String key, String subKey) {
		// TODO Auto-generated method stub
		return null;
	}

}
