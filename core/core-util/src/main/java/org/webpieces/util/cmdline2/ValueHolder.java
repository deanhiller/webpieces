package org.webpieces.util.cmdline2;

public class ValueHolder<T> {

	private T value;

	public ValueHolder(T value) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}

}
