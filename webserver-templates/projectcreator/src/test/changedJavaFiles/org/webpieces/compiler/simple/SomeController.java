package org.webpieces.compiler.simple;

import javax.inject.Singleton;

@Singleton
public class SomeController {

	public int someMethod() {
		int someReturnValue = new MyChildClass().differentMethodName();
		return someReturnValue;
	}
	
}
