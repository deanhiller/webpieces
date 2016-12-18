package org.webpieces.router.impl.params;

import java.lang.reflect.Type;

public interface Meta {

	Type getParameterizedType();

	Class<?> getFieldClass();

	void validateNullValue();
}
