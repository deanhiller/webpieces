package org.webpieces.compiler.child;

public class GrandfatherController {

	public int someMethod() {
		int someReturnValue = new Child().someValue();
		return someReturnValue;
	}
	
}
