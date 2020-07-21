package org.webpieces.webserver.json.app;

import javax.validation.constraints.NotBlank;

public class SearchRequest {

	private SearchMeta meta;
	private String query;
	
	@NotBlank
	private String testValidation;

	public SearchMeta getMeta() {
		return meta;
	}
	public void setMeta(SearchMeta meta) {
		this.meta = meta;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public String getTestValidation() {
		return testValidation;
	}
	public void setTestValidation(String testValidation) {
		this.testValidation = testValidation;
	}
	

	
}
