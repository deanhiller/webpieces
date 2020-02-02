package org.webpieces.router.impl.params;

import java.lang.reflect.Type;

public class GenericMeta<T> implements Meta {

	private Class<T> type2;

	public GenericMeta(Class<T> type2) {
		this.type2 = type2;
	}

	@Override
	public Type getParameterizedType() {
		return type2;
	}

	@Override
	public Class<T> getFieldClass() {
		return type2;
	}

	@Override
	public void validateNullValue() {
		throw new UnsupportedOperationException("If this happens, let us know.  I don't think this case is possible");
	}

	@Override
	public String toString() {
		return "GenericMeta("+type2.getName()+")";
	}
}
