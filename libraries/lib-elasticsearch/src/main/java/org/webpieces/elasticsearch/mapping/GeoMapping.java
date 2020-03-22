package org.webpieces.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;

public class GeoMapping implements PropertyMapping {

	private String type = "geo_point";
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean ignore_malformed;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean ignore_z_value;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String null_value;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Boolean getIgnore_malformed() {
		return ignore_malformed;
	}
	public void setIgnore_malformed(Boolean ignore_malformed) {
		this.ignore_malformed = ignore_malformed;
	}
	public Boolean getIgnore_z_value() {
		return ignore_z_value;
	}
	public void setIgnore_z_value(Boolean ignore_z_value) {
		this.ignore_z_value = ignore_z_value;
	}
	public String getNull_value() {
		return null_value;
	}
	public void setNull_value(String null_value) {
		this.null_value = null_value;
	}

}
