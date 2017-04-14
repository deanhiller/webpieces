package org.webpieces.ctx.api;

public class Value {

	private String value;
	
	public Value(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "Value [value=" + value + "]";
	}

}
