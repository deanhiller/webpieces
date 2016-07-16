package org.webpieces.webserver.basic.biz;

import java.util.concurrent.CompletableFuture;

public class InternalSvrErrorLib {

	public CompletableFuture<Integer> someBusinessLogic() {
		return CompletableFuture.completedFuture(33);
	}

}
