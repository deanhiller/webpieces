package org.webpieces.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GeoMapping implements PropertyMapping {

	private String type = "geo_point";
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("ignore_malformed")
	private Boolean ignoreMalformed;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("ignore_z_value")
	private Boolean ignoreZValue;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("null_value")
	private String nullValue;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Boolean getIgnoreMalformed() {
		return ignoreMalformed;
	}
	public void setIgnoreMalformed(Boolean ignoreMalformed) {
		this.ignoreMalformed = ignoreMalformed;
	}
	public Boolean getIgnoreZValue() {
		return ignoreZValue;
	}
	public void setIgnoreZValue(Boolean ignoreZValue) {
		this.ignoreZValue = ignoreZValue;
	}
	public String getNullValue() {
		return nullValue;
	}
	public void setNullValue(String nullValue) {
		this.nullValue = nullValue;
	}

}
