package org.webpieces.router.api.exceptions;

import com.webpieces.http2parser.api.dto.StatusCode;

public class BadClientRequestException extends HttpException {

	private static final long serialVersionUID = 8725117695723001888L;

	public BadClientRequestException() {
		super(StatusCode.HTTP_400_BADREQUEST);
	}

	public BadClientRequestException(String message, Throwable cause) {
		super(StatusCode.HTTP_400_BADREQUEST, message, cause);
	}

	public BadClientRequestException(String message) {
		super(StatusCode.HTTP_400_BADREQUEST, message);
	}

	public BadClientRequestException(Throwable cause) {
		super(StatusCode.HTTP_400_BADREQUEST, cause);
	}
}
