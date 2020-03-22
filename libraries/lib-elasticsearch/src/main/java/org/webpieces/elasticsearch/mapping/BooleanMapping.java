package org.webpieces.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;

public class BooleanMapping extends AbstractMapping implements PropertyMapping {

	private String type = "boolean";
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean doc_values;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String null_value;
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	public String getNull_value() {
		return null_value;
	}
	public void setNull_value(String null_value) {
		this.null_value = null_value;
	}
	public Boolean getDoc_values() {
		return doc_values;
	}
	public void setDoc_values(Boolean doc_values) {
		this.doc_values = doc_values;
	}
}
