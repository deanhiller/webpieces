package org.webpieces.http.exception;

import org.webpieces.http.StatusCode;

public class GatewayTimeoutException extends HttpServerErrorException {

	private static final long serialVersionUID = 7804145831639203745L;

	public GatewayTimeoutException() {
		super(StatusCode.HTTP_504_GATEWAY_TIMEOUT);
	}

	public GatewayTimeoutException(String message, Throwable cause) {
		super(StatusCode.HTTP_504_GATEWAY_TIMEOUT, message, cause);
	}

	public GatewayTimeoutException(String message) {
		super(StatusCode.HTTP_504_GATEWAY_TIMEOUT, message);
	}

	public GatewayTimeoutException(Throwable cause) {
		super(StatusCode.HTTP_504_GATEWAY_TIMEOUT, cause);
	}

}
