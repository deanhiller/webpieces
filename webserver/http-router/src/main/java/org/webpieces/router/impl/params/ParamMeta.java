package org.webpieces.router.impl.params;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import org.webpieces.router.api.routing.Param;

public class ParamMeta implements Meta {

	private Parameter paramMeta;

	public ParamMeta(Parameter paramMeta) {
		this.paramMeta = paramMeta;
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

}
