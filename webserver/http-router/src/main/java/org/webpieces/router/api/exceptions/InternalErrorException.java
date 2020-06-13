package org.webpieces.router.api.exceptions;

import com.webpieces.http2.api.dto.lowlevel.StatusCode;

public class InternalErrorException extends HttpException {

	private static final long serialVersionUID = 7804145831639203745L;

	public InternalErrorException() {
		super(StatusCode.HTTP_500_INTERNAL_SVR_ERROR);
	}

	public InternalErrorException(String message, Throwable cause) {
		super(StatusCode.HTTP_500_INTERNAL_SVR_ERROR, message, cause);
	}

	public InternalErrorException(String message) {
		super(StatusCode.HTTP_500_INTERNAL_SVR_ERROR, message);
	}

	public InternalErrorException(Throwable cause) {
		super(StatusCode.HTTP_500_INTERNAL_SVR_ERROR, cause);
	}

}
