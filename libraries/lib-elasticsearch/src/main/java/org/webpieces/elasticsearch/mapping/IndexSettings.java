package org.webpieces.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class IndexSettings {

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("number_of_shards")
	public Integer numberOfShards;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("number_of_replicas")
	public Integer numberOfReplicas;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Analysis analysis;

	public Analysis getAnalysis() {
		return analysis;
	}

	public void setAnalysis(Analysis analysis) {
		this.analysis = analysis;
	}
}
