package org.webpieces.elasticsearch.mapping;

import java.util.Map;

public class SingleType {

	private Map<String, PropertyMapping> properties;

	public Map<String, PropertyMapping> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, PropertyMapping> properties) {
		this.properties = properties;
	}
	
	
}
