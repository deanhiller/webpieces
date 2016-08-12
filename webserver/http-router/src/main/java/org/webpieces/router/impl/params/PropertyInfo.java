package org.webpieces.router.impl.params;

import java.util.function.Consumer;

public class PropertyInfo {

	private Class<?> propertyType;
	private Consumer<Object> propertyFunction;

	public Class<?> getPropertyType() {
		return propertyType;
	}

	public void setPropertyValue(Object translatedValue) {
		propertyFunction.accept(translatedValue);
	}
	
}
