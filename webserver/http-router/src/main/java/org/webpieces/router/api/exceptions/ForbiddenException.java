package org.webpieces.router.api.exceptions;

import com.webpieces.http2.api.dto.lowlevel.StatusCode;

public class ForbiddenException extends HttpException {

	private static final long serialVersionUID = 8725117695723001888L;

	public ForbiddenException() {
		super(StatusCode.HTTP_403_FORBIDDEN);
	}

	public ForbiddenException(String message, Throwable cause) {
		super(StatusCode.HTTP_403_FORBIDDEN, message, cause);
	}

	public ForbiddenException(String message) {
		super(StatusCode.HTTP_403_FORBIDDEN, message);
	}

	public ForbiddenException(Throwable cause) {
		super(StatusCode.HTTP_403_FORBIDDEN, cause);
	}
}
