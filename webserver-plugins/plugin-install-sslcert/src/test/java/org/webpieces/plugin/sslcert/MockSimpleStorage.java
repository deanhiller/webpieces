package org.webpieces.plugin.sslcert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.webpieces.util.futures.XFuture;

import org.webpieces.router.api.extensions.SimpleStorage;

public class MockSimpleStorage implements SimpleStorage {

	private List<XFuture<Map<String, String>>> readResponses = new ArrayList<>();

	@Override
	public XFuture<Void> save(String key, Map<String, String> properties) {
		
		return null;
	}

	@Override
	public XFuture<Map<String, String>> read(String key) {
		return readResponses.remove(0);
	}

	public void addReadResponse(XFuture<Map<String, String>> future) {
		readResponses.add(future);
	}

	@Override
	public XFuture<Void> save(String key, String subKey, String value) {
		return null;
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
