package org.webpieces.http.exception;

import org.webpieces.http.StatusCode;

public abstract class HttpServerErrorException extends HttpException {

    private static final long serialVersionUID = 5443766851653103452L;

    public HttpServerErrorException(StatusCode code) {
        super(code);
    }

    public HttpServerErrorException(StatusCode code, String message) {
        super(code, message);
    }

    public HttpServerErrorException(int code, String message) {
        super(code, message);
    }

    public HttpServerErrorException(StatusCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public HttpServerErrorException(StatusCode code, Throwable cause) {
        super(code, cause);
    }

}
