package org.webpieces.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;

public class CompletionMapping implements PropertyMapping {

	private String type = "completion";
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String analyzer;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String search_analyzer;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean preserve_separators;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean preserve_position_increments;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer max_input_length;
	
	public String getAnalyzer() {
		return analyzer;
	}
	public void setAnalyzer(String analyzer) {
		this.analyzer = analyzer;
	}
	public String getSearch_analyzer() {
		return search_analyzer;
	}
	public void setSearch_analyzer(String search_analyzer) {
		this.search_analyzer = search_analyzer;
	}
	public Boolean getPreserve_separators() {
		return preserve_separators;
	}
	public void setPreserve_separators(Boolean preserve_separators) {
		this.preserve_separators = preserve_separators;
	}
	public Boolean getPreserve_position_increments() {
		return preserve_position_increments;
	}
	public void setPreserve_position_increments(Boolean preserve_position_increments) {
		this.preserve_position_increments = preserve_position_increments;
	}
	public Integer getMax_input_length() {
		return max_input_length;
	}
	public void setMax_input_length(Integer max_input_length) {
		this.max_input_length = max_input_length;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	
}
