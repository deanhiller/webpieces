package org.webpieces.compiler.addfile;

import javax.inject.Singleton;

@Singleton
public class AddFileController {

	public int someMethod() {
		int someReturnValue = new MyAddedClass().returnSomeValue();
		return someReturnValue;
	}
	
}
