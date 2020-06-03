package org.webpieces.router.api.exceptions;

import com.webpieces.http2.api.dto.lowlevel.StatusCode;

/**
 * @deprecated Use ForbiddenException instead
 */
@Deprecated
public class AuthorizationException extends HttpException {

	private static final long serialVersionUID = 8725117695723001888L;

	public AuthorizationException() {
		super(StatusCode.HTTP_403_FORBIDDEN);
	}

	public AuthorizationException(String message, Throwable cause) {
		super(StatusCode.HTTP_403_FORBIDDEN, message, cause);
	}

	public AuthorizationException(String message) {
		super(StatusCode.HTTP_403_FORBIDDEN, message);
	}

	public AuthorizationException(Throwable cause) {
		super(StatusCode.HTTP_403_FORBIDDEN, cause);
	}
}
