package org.webpieces.plugin.json;

import java.util.regex.Pattern;

public class JsonConfig {

	private Pattern packageFilter;
	private Pattern filterPattern;

	public JsonConfig(Pattern pattern) {
		this.filterPattern = pattern;
	}

	public JsonConfig(Pattern pattern, Pattern packageFilter) {
		this.filterPattern = pattern;
		this.packageFilter = packageFilter;
	}
	
	public Pattern getFilterPattern() {
		return filterPattern;
	}

	public Pattern getPackageFilter() {
		return packageFilter;
	}
}
