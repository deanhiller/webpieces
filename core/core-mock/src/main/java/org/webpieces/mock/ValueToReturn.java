package org.webpieces.mock;

import java.util.function.Supplier;

public class ValueToReturn {

	private Supplier<?> toReturn;

	/**
	 * Supplier can throw exception toooooo!!!!
	 * @param toReturn
	 */
	public ValueToReturn(Supplier<?> toReturn) {
		this.toReturn = toReturn;
	}
	
	public Object returnOrThrowValue() {
		return toReturn.get();
	}

}
