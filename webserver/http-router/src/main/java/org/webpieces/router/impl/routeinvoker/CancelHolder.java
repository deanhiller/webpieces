package org.webpieces.router.impl.routeinvoker;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.webpieces.http2.api.dto.lowlevel.CancelReason;

class CancelHolder implements Function<CancelReason, CompletableFuture<Void>> {
	private CompletableFuture<?> controllerFuture;
	
	public void setControllerFutureResponse(CompletableFuture<?> controllerFuture) {
		this.controllerFuture = controllerFuture;
	}

	@Override
	public CompletableFuture<Void> apply(CancelReason t) {
		if(controllerFuture != null)
			controllerFuture.cancel(false);
		return CompletableFuture.completedFuture(null);
	}
}