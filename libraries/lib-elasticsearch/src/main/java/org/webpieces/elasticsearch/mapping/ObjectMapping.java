package org.webpieces.elasticsearch.mapping;

import java.util.HashMap;
import java.util.Map;

public class ObjectMapping implements PropertyMapping {

    private Map<String, PropertyMapping> properties = new HashMap<String, PropertyMapping>();

	public Map<String, PropertyMapping> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, PropertyMapping> properties) {
		this.properties = properties;
	}

}
