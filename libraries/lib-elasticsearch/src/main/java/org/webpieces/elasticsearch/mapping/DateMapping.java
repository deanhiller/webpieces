package org.webpieces.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DateMapping implements PropertyMapping {

	private String type = "date";
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String format;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String locale;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("ignore_malformed")
	private Boolean ignoreMalformed;
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

	public Boolean getIgnoreMalformed() {
		return ignoreMalformed;
	}

	public void setIgnoreMalformed(Boolean ignoreMalformed) {
		this.ignoreMalformed = ignoreMalformed;
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
