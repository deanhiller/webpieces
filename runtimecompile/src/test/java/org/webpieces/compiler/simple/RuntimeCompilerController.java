package org.webpieces.compiler.simple;

public class RuntimeCompilerController {

	public int someMethod() {
		int someReturnValue = new MyChildClass().someValue();
		return someReturnValue;
	}
	
}
