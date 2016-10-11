package org.webpieces.httpfrontend.api;

import org.webpieces.frontend.api.FrontendSocket;
import org.webpieces.frontend.api.RequestListener;
import org.webpieces.frontend.api.exception.HttpException;
import org.webpieces.httpparser.api.dto.HttpRequest;

public class MockRequestListener implements RequestListener {

	private boolean isClosed;

	@Override
	public void incomingRequest(FrontendSocket channel, HttpRequest req, boolean isHttps) {
	}

	@Override
	public void incomingError(FrontendSocket channel, HttpException exc) {
	}

	@Override
	public void clientOpenChannel(FrontendSocket channel) {
	}
	
	@Override
	public void clientClosedChannel(FrontendSocket channel) {
		isClosed = true;
	}

	@Override
	public void applyWriteBackPressure(FrontendSocket channel) {
	}

	@Override
	public void releaseBackPressure(FrontendSocket channel) {
	}

	public boolean isClosed() {
		return isClosed;
	}

}
