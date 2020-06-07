package org.webpieces.plugin.secure.properties;

import org.webpieces.plugin.secure.properties.beans.PropertyInfo;

public class ValueInfo {

	private PropertyInfo info;
	private Object value;
	private String valueAsString;

	public ValueInfo(PropertyInfo info, Object objectValue, String valueAsString) {
		this.info = info;
		this.value = objectValue;
		this.valueAsString = valueAsString;
	}

	public PropertyInfo getInfo() {
		return info;
	}

	public Object getValue() {
		return value;
	}

	public String getValueAsString() {
		return valueAsString;
	}
}
