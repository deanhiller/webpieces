package org.webpieces.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;

public class AbstractMapping {
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Float boost;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean index;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean store;
	
	public Float getBoost() {
		return boost;
	}
	public void setBoost(Float boost) {
		this.boost = boost;
	}
	public Boolean getIndex() {
		return index;
	}
	public void setIndex(Boolean index) {
		this.index = index;
	}
	public Boolean getStore() {
		return store;
	}
	public void setStore(Boolean store) {
		this.store = store;
	}

}
