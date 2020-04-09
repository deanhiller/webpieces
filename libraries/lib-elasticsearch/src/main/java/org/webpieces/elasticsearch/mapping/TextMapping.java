package org.webpieces.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TextMapping extends AbstractMapping implements PropertyMapping {

	private String type = "text";
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String analyzer;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("eager_global_ordinals")
	private String eagerGlobalOrdinals;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String fielddata;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("fielddata_frequency_filter")
	private String fielddataFrequencyFilter;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String fields;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("index_options")
	private String indexOptions;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("index_prefixes")
	private IndexPrefixes indexPrefixes;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String norms;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("position_increment_gap")
	private String positionIncrementGap;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("search_analyzer")
	private String searchAnalyzer;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String similarity;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("term_vector")
	private String termVector;
	
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
	public String getEagerGlobalOrdinals() {
		return eagerGlobalOrdinals;
	}
	public void setEagerGlobalOrdinals(String eagerGlobalOrdinals) {
		this.eagerGlobalOrdinals = eagerGlobalOrdinals;
	}
	public String getFielddata() {
		return fielddata;
	}
	public void setFielddata(String fielddata) {
		this.fielddata = fielddata;
	}
	public String getFielddataFrequencyFilter() {
		return fielddataFrequencyFilter;
	}
	public void setFielddataFrequencyFilter(String fielddataFrequencyFilter) {
		this.fielddataFrequencyFilter = fielddataFrequencyFilter;
	}
	public String getFields() {
		return fields;
	}
	public void setFields(String fields) {
		this.fields = fields;
	}
	public String getIndexOptions() {
		return indexOptions;
	}
	public void setIndexOptions(String indexOptions) {
		this.indexOptions = indexOptions;
	}
	public String getNorms() {
		return norms;
	}
	public void setNorms(String norms) {
		this.norms = norms;
	}
	public String getPositionIncrementGap() {
		return positionIncrementGap;
	}
	public void setPositionIncrementGap(String positionIncrementGap) {
		this.positionIncrementGap = positionIncrementGap;
	}
	public String getSearchAnalyzer() {
		return searchAnalyzer;
	}
	public void setSearchAnalyzer(String searchAnalyzer) {
		this.searchAnalyzer = searchAnalyzer;
	}
	public String getSimilarity() {
		return similarity;
	}
	public void setSimilarity(String similarity) {
		this.similarity = similarity;
	}
	public String getTermVector() {
		return termVector;
	}
	public void setTermVector(String termVector) {
		this.termVector = termVector;
	}
	public IndexPrefixes getIndexPrefixes() {
		return indexPrefixes;
	}
	public void setIndexPrefixes(IndexPrefixes indexPrefixes) {
		this.indexPrefixes = indexPrefixes;
	}
	
}
