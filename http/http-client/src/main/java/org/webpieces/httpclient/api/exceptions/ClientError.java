package org.webpieces.httpclient.api.exceptions;

// These are throws that the caller of the API will see
public class ClientError extends RuntimeException {
    public ClientError(String message) {
        super(message);
    }
}
