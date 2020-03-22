package org.webpieces.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;

public class IndexSettings {

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Integer number_of_shards;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Integer number_of_replicas;
	
}
