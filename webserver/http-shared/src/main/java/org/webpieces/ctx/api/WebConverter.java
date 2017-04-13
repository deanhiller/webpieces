package org.webpieces.ctx.api;

public interface WebConverter<T> {

	Class<T> getConverterType();
	
	T stringToObject(String value);
	
	String objectToString(T value);
	
}
