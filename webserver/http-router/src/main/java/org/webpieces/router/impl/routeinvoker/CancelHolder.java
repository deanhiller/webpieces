package org.webpieces.router.impl.routeinvoker;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.webpieces.router.api.controller.actions.Action;

import com.webpieces.http2parser.api.dto.CancelReason;

class CancelHolder implements Function<CancelReason, CompletableFuture<Void>> {
	private CompletableFuture<Action> controllerFuture;
	
	public void setControllerFutureResponse(CompletableFuture<Action> controllerFuture) {
		this.controllerFuture = controllerFuture;
		
	}

	@Override
	public CompletableFuture<Void> apply(CancelReason t) {
		if(controllerFuture != null)
			controllerFuture.cancel(false);
		return CompletableFuture.completedFuture(null);
	}
}