package org.webpieces.webserver.basic.app.biz;

import org.webpieces.util.futures.XFuture;

public class SomeOtherLib {

	public XFuture<Integer> someBusinessLogic() {
		return XFuture.completedFuture(99);
	}

	public void saveUser(UserDto user) {
		//save the user for real
	}
}
