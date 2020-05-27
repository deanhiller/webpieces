package org.webpieces.router.api.exceptions;

import com.webpieces.http2parser.api.dto.StatusCode;

public abstract class HttpException extends WebpiecesException {
	private static final long serialVersionUID = -4338007033423601133L;

	private final StatusCode code;
	
	public HttpException(StatusCode code) {
		super();
		this.code = code;
    }

    public HttpException(StatusCode code, String message) {
        super(message);
		this.code = code;
    }

    public HttpException(StatusCode code, String message, Throwable cause) {
        super(message, cause);
		this.code = code;
    }

    public HttpException(StatusCode code, Throwable cause) {
        super(cause);
		this.code = code;
    }

	public StatusCode getStatusCode() {
		return code;
	}
}
