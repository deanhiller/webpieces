package org.webpieces.webserver.basic.biz;

import java.util.concurrent.CompletableFuture;

public class NotFoundLib {

	public CompletableFuture<Integer> someBusinessLogic() {
		return CompletableFuture.completedFuture(99);
	}

}
