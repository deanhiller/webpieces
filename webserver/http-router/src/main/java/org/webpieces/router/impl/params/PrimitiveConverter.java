package org.webpieces.router.impl.params;

import java.util.function.Function;

import org.webpieces.ctx.api.WebConverter;

public class PrimitiveConverter<T> implements WebConverter<T> {

	private Function<String, T> toObj;
	private Function<T, String> toStr;
	private Class<T> clazz;

	public PrimitiveConverter(Class<T> clazz, Function<String, T> toObj, Function<T, String> toStr) {
		this.clazz = clazz;
		this.toObj = toObj;
		this.toStr = toStr;
	}

	@Override
	public Class<T> getConverterType() {
		return clazz;
	}
	
	@Override
	public T stringToObject(String value) {
		return toObj.apply(value);
	}

	@Override
	public String objectToString(T value) {
		return toStr.apply(value);
	}

}
