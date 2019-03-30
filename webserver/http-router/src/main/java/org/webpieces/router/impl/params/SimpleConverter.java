package org.webpieces.router.impl.params;

import org.webpieces.router.api.extensions.ObjectStringConverter;

public class SimpleConverter implements ObjectStringConverter<Object> {

	@Override
	public Class<Object> getConverterType() {
		throw new IllegalStateException("not supported from this type");
	}

	@Override
	public Object stringToObject(String value) {
		if(value == null)
			return null;
		
		throw new IllegalStateException("not supported from this type");
	}

	@Override
	public String objectToString(Object value) {
		if(value == null)
			return "";
		return value+"";
	}

}
