package org.webpieces.router.api.exceptions;

import com.webpieces.http2.api.dto.lowlevel.StatusCode;

public class TooManyRequestsException extends HttpException {

	private static final long serialVersionUID = 7804145831639203745L;

	public TooManyRequestsException() {
		super(StatusCode.HTTP_429_TOO_MANY_REQUESTS);
	}

	public TooManyRequestsException(String message, Throwable cause) {
		super(StatusCode.HTTP_429_TOO_MANY_REQUESTS, message, cause);
	}

	public TooManyRequestsException(String message) {
		super(StatusCode.HTTP_429_TOO_MANY_REQUESTS, message);
	}

	public TooManyRequestsException(Throwable cause) {
		super(StatusCode.HTTP_429_TOO_MANY_REQUESTS, cause);
	}

}
