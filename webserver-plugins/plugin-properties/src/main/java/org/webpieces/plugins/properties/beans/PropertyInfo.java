package org.webpieces.plugins.properties.beans;

import java.lang.reflect.Method;

public class PropertyInfo {

	private String name;
	private Method getter;
	private Method setter;
	private Object injectee;

	public PropertyInfo(String propertyName, Object injectee, Method getter, Method setter) {
		this.name = propertyName;
		this.injectee = injectee;
		this.getter = getter;
		this.setter = setter;
	}

	public String getName() {
		return name;
	}

	public Method getGetter() {
		return getter;
	}

	public Method getSetter() {
		return setter;
	}
	
	public boolean isReadOnly() {
		if(setter == null)
			return true;
		return false;
	}

	public Object getInjectee() {
		return injectee;
	}

}
