package org.webpieces.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;

public class DateMapping implements PropertyMapping {

	private String type = "date";
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String format;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String locale;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean ignore_malformed;
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

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public Boolean getIgnore_malformed() {
		return ignore_malformed;
	}

	public void setIgnore_malformed(Boolean ignore_malformed) {
		this.ignore_malformed = ignore_malformed;
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
