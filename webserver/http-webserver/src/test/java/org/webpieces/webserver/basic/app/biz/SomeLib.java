package org.webpieces.webserver.basic.app.biz;

import org.webpieces.util.futures.XFuture;

public class SomeLib {

	public XFuture<Integer> someBusinessLogic() {
		return XFuture.completedFuture(33);
	}

	public void validateUser(UserDto user) {
	}
}
