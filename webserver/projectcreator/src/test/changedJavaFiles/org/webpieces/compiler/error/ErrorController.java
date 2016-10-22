package org.webpieces.compiler.error;

import javax.inject.Singleton;

/**
 * This has no error right now but the test will edit it(copy over it) so it has an error and hot-compile
 */
@Singleton
public class ErrorController {

	public int someMethod() {
		int someReturnValue = new ChildClassNoError().noMethodExists();
		return someReturnValue;
	}
	
}
