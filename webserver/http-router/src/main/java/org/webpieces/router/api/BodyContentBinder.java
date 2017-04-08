package org.webpieces.router.api;

import java.lang.annotation.Annotation;

import org.webpieces.router.api.actions.RenderContent;

public interface BodyContentBinder {

	<T> boolean isManaged(Class<T> entityClass, Class<? extends Annotation> paramAnnotation);

	<T> T unmarshal(Class<T> paramTypeToCreate, byte[] data);

	<T> RenderContent marshal(T bean);

	Class<? extends Annotation> getAnnotation();
	
}
