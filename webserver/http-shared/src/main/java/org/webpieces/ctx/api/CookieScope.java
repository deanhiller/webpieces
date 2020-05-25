package org.webpieces.ctx.api;

public interface CookieScope {

	String getName();

	void clear();
	
	boolean containsKey(String key);
	
	/**
	 * Uses the platform's ObjectTranslator(which you can override) to translate the value
	 * from Object to String to be put in the session cookie
	 * 
	 * @param key
	 * @param value
	 */
	void put(String key, Object value);

	<T> T remove(String key, Class<T> type);
	
	<T> T get(String key, Class<T> type);

	/**
	 * Gets the raw value in the map with no translation
	 * 
	 * @param key
	 * @return
	 */
	String get(String key);
	
	String remove(String key);

}
