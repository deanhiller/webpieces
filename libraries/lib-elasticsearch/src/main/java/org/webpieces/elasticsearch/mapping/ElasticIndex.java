package org.webpieces.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;

public class ElasticIndex {

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private IndexSettings settings;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Mappings mappings;

	public IndexSettings getSettings() {
		return settings;
	}
	public void setSettings(IndexSettings settings) {
		this.settings = settings;
	}
	public Mappings getMappings() {
		return mappings;
	}
	public void setMappings(Mappings mappings) {
		this.mappings = mappings;
	}

}
