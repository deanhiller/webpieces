package org.webpieces.compiler.simple;

public class SomeController {

	public int someMethod() {
		int someReturnValue = new MyChildClass().someValue();
		return someReturnValue;
	}
	
}
