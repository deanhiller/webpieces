package org.webpieces.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IndexPrefixes {

	@JsonProperty("min_chars")
	private int minChars;
	@JsonProperty("max_chars")
	private int maxChars;
	
	public int getMinChars() {
		return minChars;
	}
	public void setMinChars(int minChars) {
		this.minChars = minChars;
	}
	public int getMaxChars() {
		return maxChars;
	}
	public void setMaxChars(int maxChars) {
		this.maxChars = maxChars;
	}
	
}
