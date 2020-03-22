package org.webpieces.elasticsearch.actions;

public class AliasChange {

	private String alias;
	private String index;

	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
}
