package org.webpieces.compiler.bytecache;

import org.webpieces.compiler.impl.test.ForTestAction;

public class ByteCacheController {

	public void notFound() {
	}
	
	public ForTestAction createUserForm() {
		return new ForTestAction(SomeRouteId.GET_CREATE_USER_PAGE);
	}
	
}
