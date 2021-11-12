package org.webpieces.router.api.simplesvr;

import java.util.HashMap;
import java.util.Map;
import org.webpieces.util.futures.XFuture;

import org.webpieces.router.api.extensions.SimpleStorage;

public class EmptyStorage implements SimpleStorage {

	@Override
	public XFuture<Void> save(String key, String subKey, String value) {
		
		return null;
	}

	@Override
	public XFuture<Void> save(String key, Map<String, String> properties) {
		
		return null;
	}

	@Override
	public XFuture<Map<String, String>> read(String key) {
		return XFuture.completedFuture(new HashMap<>());
	}

	@Override
	public XFuture<Void> delete(String key) {
		
		return null;
	}

	@Override
	public XFuture<Void> delete(String key, String subKey) {
		
		return null;
	}

}
