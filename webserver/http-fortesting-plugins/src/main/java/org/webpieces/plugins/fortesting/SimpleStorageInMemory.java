package org.webpieces.plugins.fortesting;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.router.api.SimpleStorage;

public class SimpleStorageInMemory implements SimpleStorage {

	private Map<String, Map<String, String>> db = new HashMap<>();
	
	@Override
	public CompletableFuture<Void> save(String key, String subKey, String value) {
		Map<String, String> props = getSubMap(key);
		props.put(subKey, value);
		return CompletableFuture.completedFuture(null);
	}

	private Map<String, String> getSubMap(String key) {
		Map<String, String> map = db.getOrDefault(key, new HashMap<>());
		db.put(key, map);
		return map;
	}

	@Override
	public CompletableFuture<Void> save(String key, Map<String, String> properties) {
		Map<String, String> subMap = getSubMap(key);
		subMap.putAll(properties);
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Map<String, String>> read(String key) {
		Map<String, String> props = getSubMap(key);
		return CompletableFuture.completedFuture(props);
	}

}
