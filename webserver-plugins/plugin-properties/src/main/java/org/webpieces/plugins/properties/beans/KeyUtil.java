package org.webpieces.plugins.properties.beans;

public class KeyUtil {

	public static String PLUGIN_PROPERTIES_KEY = "PLUGIN_PROPERTIES_KEY";

	public static String formKey(String category, String beanName, String propertyName) {
		String key = category+":"+beanName+":"+propertyName;
		return key;
	}
}