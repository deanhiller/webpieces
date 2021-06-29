package org.webpieces.http.exception;

import org.webpieces.http.StatusCode;

public abstract class HttpClientErrorException extends HttpException {

    private static final long serialVersionUID = -8790674181969944625L;

    public HttpClientErrorException(StatusCode code) {
        super(code);
    }

    public HttpClientErrorException(StatusCode code, String message) {
        super(code, message);
    }

    public HttpClientErrorException(int code, String message) {
        super(code, message);
    }

    public HttpClientErrorException(StatusCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public HttpClientErrorException(StatusCode code, Throwable cause) {
        super(code, cause);
    }

}
