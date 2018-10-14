package org.webpieces.plugins.properties;

import org.webpieces.plugins.properties.beans.PropertyInfo;

public class ValueInfo {

	private PropertyInfo info;
	private Object value;

	public ValueInfo(PropertyInfo info, Object objectValue) {
		this.info = info;
		this.value = objectValue;
	}

	public PropertyInfo getInfo() {
		return info;
	}

	public Object getValue() {
		return value;
	}

}
