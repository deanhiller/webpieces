package org.webpieces.router.api.extensions;

import java.lang.reflect.Type;

public interface Meta {

	Type getParameterizedType();

	Class<?> getFieldClass();

	void validateNullValue();
}
