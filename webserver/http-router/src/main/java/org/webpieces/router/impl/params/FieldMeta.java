package org.webpieces.router.impl.params;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.extensions.Meta;
import org.webpieces.util.exceptions.SneakyThrow;

public class FieldMeta implements Meta {

	private static final Logger log = LoggerFactory.getLogger(FieldMeta.class);
	private Field field;

	public FieldMeta(Field field) {
		this.field = field;
	}

	@Override
	public Type getParameterizedType() {
		return field.getGenericType();
	}

	@Override
	public Class<?> getFieldClass() {
		return field.getType();
	}

	public void setValueOnBean(Object bean, Object translatedValue) {
		BiFunction<Object, Object, Void> applyBeanValueFunction = createFunction(bean.getClass(), field);
		
		//skip setting to null if it is a primitive(allow setting null on a String 
		if(translatedValue != null)
			applyBeanValueFunction.apply(bean, translatedValue);
		else if(!getFieldClass().isPrimitive())
			applyBeanValueFunction.apply(bean, translatedValue);
	}
	
	private BiFunction<Object, Object, Void> createFunction(Class<? extends Object> beanClass, Field field) {
		String key = field.getName();
		String cap = key.substring(0, 1).toUpperCase() + key.substring(1);
		String methodName = "set"+cap;

		//What is slower....throwing exceptions or looping over methods to not through exception?....
		try {
			Method method = beanClass.getMethod(methodName, field.getType());
			return (bean, val) -> invokeMethod(method, bean, val);
		} catch (NoSuchMethodException e) {
			log.warn("performance penalty since method="+methodName+" does not exist on class="+beanClass.getName()+" using field instead to set data");
			return (bean, val) -> setField(field, bean, val);
		} catch (SecurityException e) {
			throw SneakyThrow.sneak(e);
		}
	}

	private Void setField(Field field, Object bean, Object val) {
		field.setAccessible(true);
		try {
			field.set(bean, val);
		} catch (IllegalAccessException e) {
			throw SneakyThrow.sneak(e);
		}
		return null;
	}

	private Void invokeMethod(Method method, Object bean, Object val) {
		try {
			method.invoke(bean, val);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw SneakyThrow.sneak(e);
		}
		return null;
	}

	@Override
	public String toString() {
		return "FieldMeta [field=" + field + "]";
	}

	@Override
	public void validateNullValue() {
		//by default fields are nullable(not required) UNLESS @Required annotation
		//is used
		throw new UnsupportedOperationException("need to implement/test");
	}
	
}
