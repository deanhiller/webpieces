package org.webpieces.router.api;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * SimpleStorage is a simple standard interface for Plugins to save their settings to the database
 */
public interface SimpleStorage {

	//YES, this looks ALOT like noSQL to start with so it works with nosql and RDBMS so your backend
	//storage for plugin data is what you decide you want it to be!!!
	public CompletableFuture<Void> save(String key, String subKey, String value);
	
	//OR in noSQL update many pieces of the row (or in RDBMS, this updates many rows)
	public CompletableFuture<Void> save(String key, Map<String, String> properties);

	//READ the entire row in noSQL (and in RDBMS, read all the rows that have that key)
	public CompletableFuture<Map<String, String>> read(String key);
	
}
