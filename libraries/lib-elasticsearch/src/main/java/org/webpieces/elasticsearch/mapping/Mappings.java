package org.webpieces.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;

public class Mappings {

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private SingleType doc;

	public SingleType getDoc() {
		return doc;
	}

	public void setDoc(SingleType doc) {
		this.doc = doc;
	}
	
	
}
