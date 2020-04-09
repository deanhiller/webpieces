package org.webpieces.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BooleanMapping extends AbstractMapping implements PropertyMapping {

	private String type = "boolean";
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("doc_values")
	private Boolean docValues;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("null_value")
	private String nullValue;
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	public String getNullValue() {
		return nullValue;
	}
	public void setNullValue(String nullValue) {
		this.nullValue = nullValue;
	}
	public Boolean getDocValues() {
		return docValues;
	}
	public void setDocValues(Boolean docValues) {
		this.docValues = docValues;
	}
}
