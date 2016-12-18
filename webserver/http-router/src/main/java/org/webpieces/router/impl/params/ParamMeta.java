package org.webpieces.router.impl.params;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import org.webpieces.router.api.exceptions.DataMismatchException;
import org.webpieces.router.api.routing.Nullable;
import org.webpieces.router.api.routing.Param;

public class ParamMeta implements Meta {

	private Method method;
	private Parameter paramMeta;
	private Annotation[] annotations;

	public ParamMeta(Method method, Parameter paramMeta, Annotation[] annotations) {
		this.method = method;
		this.paramMeta = paramMeta;
		this.annotations = annotations;
	}

	public String getName() {
		Param annotation = paramMeta.getAnnotation(Param.class);
		String name = paramMeta.getName();
		if(annotation != null) {
			name = annotation.value();
		}
		return name;
	}

	@Override
	public Type getParameterizedType() {
		return paramMeta.getParameterizedType();
	}

	@Override
	public Class<?> getFieldClass() {
		return paramMeta.getType();
	}

	@Override
	public String toString() {
		return "ParamMeta [paramMeta=" + paramMeta + "]";
	}

	@Override
	public void validateNullValue() {
		//by default, params are required unless some sort of @Nullable annotation
		//is used.
		for(Annotation annotation : annotations) {
			Class<? extends Annotation> annotationType = annotation.annotationType();
			if(annotationType.equals(Nullable.class))
				return; //null is allowed so just return
		}
		
		throw new DataMismatchException("On method="+method+" the parameter="+this+" did not have any @Nullable annotation so data is required but none came in");
	}

}
