package org.webpieces.webserver.basic.app.biz;

import java.util.concurrent.CompletableFuture;

public class SomeLib {

	public CompletableFuture<Integer> someBusinessLogic() {
		return CompletableFuture.completedFuture(33);
	}

	public void validateUser(UserDbo user) {
	}
}
