package org.webpieces.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;

public class TextMapping extends AbstractMapping implements PropertyMapping {

	private String type = "text";
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String analyzer;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String eager_global_ordinals;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String fielddata;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String fielddata_frequency_filter;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String fields;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String index_options;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private IndexPrefixes index_prefixes;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String norms;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String position_increment_gap;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String search_analyzer;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String similarity;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String term_vector;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getAnalyzer() {
		return analyzer;
	}
	public void setAnalyzer(String analyzer) {
		this.analyzer = analyzer;
	}
	public String getEager_global_ordinals() {
		return eager_global_ordinals;
	}
	public void setEager_global_ordinals(String eager_global_ordinals) {
		this.eager_global_ordinals = eager_global_ordinals;
	}
	public String getFielddata() {
		return fielddata;
	}
	public void setFielddata(String fielddata) {
		this.fielddata = fielddata;
	}
	public String getFielddata_frequency_filter() {
		return fielddata_frequency_filter;
	}
	public void setFielddata_frequency_filter(String fielddata_frequency_filter) {
		this.fielddata_frequency_filter = fielddata_frequency_filter;
	}
	public String getFields() {
		return fields;
	}
	public void setFields(String fields) {
		this.fields = fields;
	}
	public String getIndex_options() {
		return index_options;
	}
	public void setIndex_options(String index_options) {
		this.index_options = index_options;
	}
	public String getNorms() {
		return norms;
	}
	public void setNorms(String norms) {
		this.norms = norms;
	}
	public String getPosition_increment_gap() {
		return position_increment_gap;
	}
	public void setPosition_increment_gap(String position_increment_gap) {
		this.position_increment_gap = position_increment_gap;
	}
	public String getSearch_analyzer() {
		return search_analyzer;
	}
	public void setSearch_analyzer(String search_analyzer) {
		this.search_analyzer = search_analyzer;
	}
	public String getSimilarity() {
		return similarity;
	}
	public void setSimilarity(String similarity) {
		this.similarity = similarity;
	}
	public String getTerm_vector() {
		return term_vector;
	}
	public void setTerm_vector(String term_vector) {
		this.term_vector = term_vector;
	}
	public IndexPrefixes getIndex_prefixes() {
		return index_prefixes;
	}
	public void setIndex_prefixes(IndexPrefixes index_prefixes) {
		this.index_prefixes = index_prefixes;
	}
	
}
