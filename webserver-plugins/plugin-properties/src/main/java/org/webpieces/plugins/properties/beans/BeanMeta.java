package org.webpieces.plugins.properties.beans;

import java.util.List;

public class BeanMeta {

	private List<PropertyInfo> properties;
	private String name;
	private Class<?> interfaze;

	public BeanMeta(String name, Class<?> interfaze, List<PropertyInfo> properties) {
		this.name = name;
		this.interfaze = interfaze;
		this.properties = properties;
	}

	public List<PropertyInfo> getProperties() {
		return properties;
	}

	public String getName() {
		return name;
	}

	public Class<?> getInterface() {
		return interfaze;
	}

}
