package org.webpieces.plugin.json;

import java.util.regex.Pattern;

public class JsonConfig {

	private Pattern filterPattern;

	public JsonConfig(Pattern pattern) {
		this.filterPattern = pattern;
	}
	
	public Pattern getFilterPattern() {
		return filterPattern;
	}

}
