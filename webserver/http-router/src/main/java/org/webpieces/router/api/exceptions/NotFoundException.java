package org.webpieces.router.api.exceptions;

import com.webpieces.http2.api.dto.lowlevel.StatusCode;

public class NotFoundException extends HttpException {

	private static final long serialVersionUID = 7804145831639203745L;

	public NotFoundException() {
		super(StatusCode.HTTP_404_NOTFOUND);
	}

	public NotFoundException(String message, Throwable cause) {
		super(StatusCode.HTTP_404_NOTFOUND, message, cause);
	}

	public NotFoundException(String message) {
		super(StatusCode.HTTP_404_NOTFOUND, message);
	}

	public NotFoundException(Throwable cause) {
		super(StatusCode.HTTP_404_NOTFOUND, cause);
	}

}
