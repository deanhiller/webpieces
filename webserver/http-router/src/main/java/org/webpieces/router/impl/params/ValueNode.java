package org.webpieces.router.impl.params;

public class ValueNode extends ParamNode {

	private String value;
	private String fullKeyName;
	private FromEnum from;

	public ValueNode(String list, String fullKeyName, FromEnum from) {
		this.value = list;
		this.fullKeyName = fullKeyName;
		this.from = from;
	}

	public String getFullKeyName() {
		return fullKeyName;
	}

	public FromEnum getFrom() {
		return from;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return ""+value+":"+fullKeyName+":"+from;
	}

	public String getFullName() {
		return fullKeyName;
	}
	
	
}
