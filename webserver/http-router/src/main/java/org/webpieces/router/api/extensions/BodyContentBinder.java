package org.webpieces.router.api.extensions;

import java.lang.annotation.Annotation;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.controller.actions.RenderContent;

public interface BodyContentBinder {

	boolean canTransform(Class<?> paramClass);

	<T> T unmarshal(RequestContext ctx, ParamMeta paramTypeToCreate, byte[] data);

	<T> RenderContent marshal(T bean);

	Class<? extends Annotation> getAnnotation();


}
