package org.webpieces.webserver.basic.app.biz;

import java.util.concurrent.CompletableFuture;

public class SomeOtherLib {

	public CompletableFuture<Integer> someBusinessLogic() {
		return CompletableFuture.completedFuture(99);
	}

	public void saveUser(UserDto user) {
		//save the user for real
	}
}
