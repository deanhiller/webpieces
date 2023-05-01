package org.webpieces.http.exception;

import org.webpieces.http.StatusCode;

public class BadGatewayException extends HttpServerErrorException {

	private static final long serialVersionUID = 7804145831639203745L;
	private Object jsonError;

	public BadGatewayException() {
		super(StatusCode.HTTP_502_BAD_GATEWAY);
	}

	public BadGatewayException(String message, Throwable cause) {
		super(StatusCode.HTTP_502_BAD_GATEWAY, message, cause);
	}

	public BadGatewayException(String message) {
		this(message, null);
	}
	public BadGatewayException(String message, Object jsonError) {
		super(StatusCode.HTTP_502_BAD_GATEWAY, message);
		this.jsonError = jsonError;
	}

	public BadGatewayException(Throwable cause) {
		super(StatusCode.HTTP_502_BAD_GATEWAY, cause);
	}

	public Object getJsonError() {
		return jsonError;
	}
}
