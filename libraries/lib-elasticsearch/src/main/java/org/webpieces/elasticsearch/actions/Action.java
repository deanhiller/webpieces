package org.webpieces.elasticsearch.actions;

import com.fasterxml.jackson.annotation.JsonInclude;

public class Action {

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private AliasChange remove;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private AliasChange add;

	public Action() {
	}
	public Action(AliasChange change, boolean isAdd) {
		if(isAdd)
			add = change;
		else
			remove = change;
	}
	
	public AliasChange getRemove() {
		return remove;
	}
	public void setRemove(AliasChange remove) {
		this.remove = remove;
	}
	public AliasChange getAdd() {
		return add;
	}
	public void setAdd(AliasChange add) {
		this.add = add;
	}
	
	
}
