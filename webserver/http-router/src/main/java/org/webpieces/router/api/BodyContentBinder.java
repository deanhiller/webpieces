package org.webpieces.router.api;

import java.lang.annotation.Annotation;

public interface BodyContentBinder {

	<T> boolean isManaged(Class<T> entityClass, Class<? extends Annotation> paramAnnotation);

	<T> T unmarshal(Class<T> paramTypeToCreate, byte[] data);

	<T> byte[] marshal(T bean);
	
}
