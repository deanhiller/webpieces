package org.webpieces.router.api.exceptions;

import com.webpieces.http2.api.dto.lowlevel.StatusCode;

public class GatewayTimeoutException extends HttpException {

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
