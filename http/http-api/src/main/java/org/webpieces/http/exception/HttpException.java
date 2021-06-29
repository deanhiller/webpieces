package org.webpieces.http.exception;

import org.webpieces.http.StatusCode;
import org.webpieces.util.exceptions.WebpiecesException;

public abstract class HttpException extends WebpiecesException {

	private static final long serialVersionUID = -4338007033423601133L;

	private final StatusCode statusCode;
	private final int code;

	public HttpException(StatusCode code) {
		this.statusCode = code;
		this.code = code.getCode();
    }

    public HttpException(StatusCode code, String message) {
        super(message);
		this.statusCode = code;
		this.code = code.getCode();
    }

    public HttpException(int code, String message) {
        super(message);
		this.code = code;
		this.statusCode = StatusCode.lookup(code);
    }
    
    public HttpException(StatusCode code, String message, Throwable cause) {
        super(message, cause);
		this.statusCode = code;
		this.code = code.getCode();
    }

    public HttpException(StatusCode code, Throwable cause) {
        super(cause);
		this.statusCode = code;
		this.code = code.getCode();
    }

    public int getHttpCode() {
    	return code;
    }
    
	public StatusCode getStatusCode() {
		return statusCode;
	}

	public String getStatusMessage() {
		return (statusCode != null) ? statusCode.getReason() : "";
	}

}
