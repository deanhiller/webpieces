package org.webpieces.router.api;

import org.webpieces.router.api.dto.Response;

public interface ResponseStreamer {

	void sendRedirect(Response httpResponse);

	void failure(Throwable e);

}
