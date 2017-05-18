package org.webpieces.frontend2.api;

public interface StreamSession {

	public void put(Object key, Object value);
	
	public Object get(Object key);
	
}
