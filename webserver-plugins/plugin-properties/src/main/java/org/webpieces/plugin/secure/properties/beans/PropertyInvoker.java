package org.webpieces.plugin.secure.properties.beans;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.api.extensions.ObjectStringConverter;
import org.webpieces.router.impl.params.ObjectTranslator;
import org.webpieces.util.exceptions.SneakyThrow;

@Singleton
public class PropertyInvoker {

	private ObjectTranslator objectTranslator;

	@Inject
	public PropertyInvoker(
		ObjectTranslator objectTranslator
	) {
		this.objectTranslator = objectTranslator;
	}
	
	public String readPropertyAsString(PropertyInfo p) {
		Object value = getValue(p);
		return objectTranslator.getConverterFor(value).objectToString(value);
	}
	
	private Object getValue(PropertyInfo p) {
		Method method = p.getGetter();
		try {
			return method.invoke(p.getInjectee());
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw SneakyThrow.sneak(e);
		}
	}

	public ObjectStringConverter<?> fetchConverter(PropertyInfo info) {
		Class<?> returnType = info.getGetter().getReturnType();
		ObjectStringConverter<?> converter = objectTranslator.getConverter(returnType);
		if(converter == null) {
			//since we checked before when loading, there should be a converter, or code was changed and we have new bug
			throw new RuntimeException("Odd, this shouldn't be possible, bug.  return type="+returnType.getName());
		}
		return converter;
	}

	public void writeProperty(PropertyInfo info, String valueAsStr) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> returnType = info.getGetter().getReturnType();
		ObjectStringConverter<?> converter = objectTranslator.getConverter(returnType);
		if(converter == null) {
			//since we checked before when loading, there should be a converter, or code was changed and we have new bug
			throw new RuntimeException("Odd, this shouldn't be possible, bug.  return type="+returnType.getName());
		}
		Object val = converter.stringToObject(valueAsStr);
		Object injectee = info.getInjectee();
		info.getSetter().invoke(injectee, val);
	}

}
