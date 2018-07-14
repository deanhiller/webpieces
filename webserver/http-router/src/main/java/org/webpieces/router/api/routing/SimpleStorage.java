package org.webpieces.router.api.routing;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * SimpleStorage is a simple standard interface for Plugins to save their settings to the database
 */
public interface SimpleStorage {

	public CompletableFuture<Void> save(String key, Map<String, String> properties);
	
	public CompletableFuture<Map<String, String>> read(String key);
	
}
