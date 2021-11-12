package org.webpieces.plugins.fortesting;

import java.util.HashMap;
import java.util.Map;
import org.webpieces.util.futures.XFuture;

import org.webpieces.router.api.extensions.SimpleStorage;

public class SimpleStorageInMemory implements SimpleStorage {

	private Map<String, Map<String, String>> db = new HashMap<>();
	
	@Override
	public XFuture<Void> save(String key, String subKey, String value) {
		Map<String, String> props = getSubMap(key);
		props.put(subKey, value);
		return XFuture.completedFuture(null);
	}

	private Map<String, String> getSubMap(String key) {
		Map<String, String> map = db.getOrDefault(key, new HashMap<>());
		db.put(key, map);
		return map;
	}

	@Override
	public XFuture<Void> save(String key, Map<String, String> properties) {
		Map<String, String> subMap = getSubMap(key);
		subMap.putAll(properties);
		return XFuture.completedFuture(null);
	}

	@Override
	public XFuture<Map<String, String>> read(String key) {
		Map<String, String> props = getSubMap(key);
		return XFuture.completedFuture(props);
	}

	@Override
	public XFuture<Void> delete(String key) {
		db.remove(key);
		return XFuture.completedFuture(null);
	}

	@Override
	public XFuture<Void> delete(String key, String subKey) {
		Map<String, String> subMap = getSubMap(key);
		subMap.remove(subKey);
		return XFuture.completedFuture(null);
	}

}
