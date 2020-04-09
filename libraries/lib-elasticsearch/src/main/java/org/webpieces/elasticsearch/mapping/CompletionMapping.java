package org.webpieces.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CompletionMapping implements PropertyMapping {

	private String type = "completion";
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String analyzer;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("search_analyzer")
	private String searchAnalyzer;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("preserve_separators")
	private Boolean preserveSeparators;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("preserve_position_increments")
	private Boolean preservePositionIncrements;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("max_input_length")
	private Integer maxInputLength;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private List<Context> contexts;

	public String getAnalyzer() {
		return analyzer;
	}
	public void setAnalyzer(String analyzer) {
		this.analyzer = analyzer;
	}
	public String getSearchAnalyzer() {
		return searchAnalyzer;
	}
	public void setSearchAnalyzer(String searchAnalyzer) {
		this.searchAnalyzer = searchAnalyzer;
	}
	public Boolean getPreserveSeparators() {
		return preserveSeparators;
	}
	public void setPreserveSeparators(Boolean preserveSeparators) {
		this.preserveSeparators = preserveSeparators;
	}
	public Boolean getPreservePositionIncrements() {
		return preservePositionIncrements;
	}
	public void setPreservePositionIncrements(Boolean preservePositionIncrements) {
		this.preservePositionIncrements = preservePositionIncrements;
	}
	public Integer getMaxInputLength() {
		return maxInputLength;
	}
	public void setMaxInputLength(Integer maxInputLength) {
		this.maxInputLength = maxInputLength;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}


	public List<Context> getContexts() {
		return contexts;
	}

	public void setContexts(List<Context> contexts) {
		this.contexts = contexts;
	}
}
