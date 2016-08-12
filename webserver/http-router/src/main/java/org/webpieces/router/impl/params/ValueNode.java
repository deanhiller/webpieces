package org.webpieces.router.impl.params;

import java.util.List;

public class ValueNode extends ParamNode {

	private List<String> value;
	private String fullKeyName;

	public ValueNode(List<String> list, String fullKeyName) {
		this.value = list;
		this.fullKeyName = fullKeyName;
	}

	public List<String> getValue() {
		return value;
	}

	@Override
	public String toString() {
		return ""+value+":"+fullKeyName;
	}

	public String getFullName() {
		return fullKeyName;
	}
}
