package org.webpieces.plugin.json;

import java.util.regex.Pattern;

public class JsonConfig {

	private Pattern filterPattern;
	private boolean isNotFoundFilter;

	public JsonConfig(Pattern pattern, boolean isNotFoundFilter) {
		this.filterPattern = pattern;
		this.isNotFoundFilter = isNotFoundFilter;
	}
	
	public Pattern getFilterPattern() {
		return filterPattern;
	}

	public boolean isNotFoundFilter() {
		return isNotFoundFilter;
	}

}
