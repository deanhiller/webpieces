package org.webpieces.router.api.exceptions;

import com.webpieces.http2.api.dto.lowlevel.StatusCode;

/**
 * @deprecated Use BadClientRequestException instead!!
 */
@Deprecated
public class ClientDataError extends HttpException {

	private static final long serialVersionUID = 8725117695723001888L;

	public ClientDataError() {
		super(StatusCode.HTTP_400_BADREQUEST);
	}

	public ClientDataError(String message, Throwable cause) {
		super(StatusCode.HTTP_400_BADREQUEST, message, cause);
	}

	public ClientDataError(String message) {
		super(StatusCode.HTTP_400_BADREQUEST, message);
	}

	public ClientDataError(Throwable cause) {
		super(StatusCode.HTTP_400_BADREQUEST, cause);
	}
}
