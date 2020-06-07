package org.webpieces.plugin.secure.properties.beans;

public class KeyUtil {

	public static final String PLUGIN_PROPERTIES_KEY = "PLUGIN_PROPERTIES_KEY";

	public static String formKey(String category, String beanName, String propertyName) {
		String key = category+":"+beanName+":"+propertyName;
		return key;
	}
}
