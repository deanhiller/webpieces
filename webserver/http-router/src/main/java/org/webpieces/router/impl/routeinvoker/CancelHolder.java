package org.webpieces.router.impl.routeinvoker;

import org.webpieces.util.futures.XFuture;
import java.util.function.Function;

import com.webpieces.http2.api.dto.lowlevel.CancelReason;

class CancelHolder implements Function<CancelReason, XFuture<Void>> {
	private XFuture<?> controllerFuture;
	
	public void setControllerFutureResponse(XFuture<?> controllerFuture) {
		this.controllerFuture = controllerFuture;
	}

	@Override
	public XFuture<Void> apply(CancelReason t) {
		if(controllerFuture != null)
			controllerFuture.cancel(false);
		return XFuture.completedFuture(null);
	}
}