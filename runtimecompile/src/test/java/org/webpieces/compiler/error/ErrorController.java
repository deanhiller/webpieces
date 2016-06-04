package org.webpieces.compiler.error;

/**
 * This has no error right now but the test will edit it(copy over it) so it has an error and hot-compile
 */
public class ErrorController {

	public int someMethod() {
		int someReturnValue = new ChildClassNoError().someValue();
		return someReturnValue;
	}
	
}
