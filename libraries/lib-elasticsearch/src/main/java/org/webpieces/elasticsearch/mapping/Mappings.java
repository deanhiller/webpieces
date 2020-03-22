package org.webpieces.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;

public class Mappings {

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private SingleType _doc;

	public SingleType get_doc() {
		return _doc;
	}

	public void set_doc(SingleType _doc) {
		this._doc = _doc;
	}
	
	
}
