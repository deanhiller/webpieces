package org.webpieces.router.api.exceptions;

import java.util.concurrent.CompletionException;

public abstract class HttpException extends CompletionException {
    public HttpException() {
    }

    public HttpException(String message) {
        super(message);
    }

    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpException(Throwable cause) {
        super(cause);
    }
}
