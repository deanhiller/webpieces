package org.webpieces.router.api.exceptions;

import com.webpieces.http2parser.api.dto.StatusCode;

public class BadGatewayException extends HttpException {

	private static final long serialVersionUID = 7804145831639203745L;

	public BadGatewayException() {
		super(StatusCode.HTTP_502_BAD_GATEWAY);
	}

	public BadGatewayException(String message, Throwable cause) {
		super(StatusCode.HTTP_502_BAD_GATEWAY, message, cause);
	}

	public BadGatewayException(String message) {
		super(StatusCode.HTTP_502_BAD_GATEWAY, message);
	}

	public BadGatewayException(Throwable cause) {
		super(StatusCode.HTTP_502_BAD_GATEWAY, cause);
	}

}
