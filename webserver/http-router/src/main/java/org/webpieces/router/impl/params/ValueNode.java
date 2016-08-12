package org.webpieces.router.impl.params;

import java.util.List;

public class ValueNode extends ParamNode {

	private List<String> value;

	public ValueNode(List<String> list) {
		this.value = list;
	}


	public List<String> getValue() {
		return value;
	}
}
