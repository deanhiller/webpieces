package org.webpieces.router.impl.params;

public class ValueNode extends ParamNode {

	private String[] value;

	public ValueNode(String[] value) {
		this.value = value;
	}


	public String[] getValue() {
		return value;
	}
}
