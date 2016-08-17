package org.webpieces.router.impl.params;

import java.lang.reflect.Type;

public class GenericMeta implements Meta {

	private Class type2;

	public GenericMeta(Class type2) {
		this.type2 = type2;
	}

	@Override
	public Type getParameterizedType() {
		return type2;
	}

	@Override
	public Class<?> getFieldClass() {
		return type2;
	}

}
