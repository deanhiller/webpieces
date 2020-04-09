package org.webpieces.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

public class Mappings {

	private Map<String, PropertyMapping> properties;

	public Map<String, PropertyMapping> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, PropertyMapping> properties) {
		this.properties = properties;
	}
	
}
