package org.webpieces.http.exception;

import org.webpieces.http.StatusCode;

public class BadCustomerRequestException extends HttpClientErrorException {

	private static final long serialVersionUID = 8725117695723001888L;

	public BadCustomerRequestException() {
		super(StatusCode.HTTP_491_BAD_CUSTOMER_REQUEST);
	}

	public BadCustomerRequestException(String message, Throwable cause) {
		super(StatusCode.HTTP_491_BAD_CUSTOMER_REQUEST, message, cause);
	}

	public BadCustomerRequestException(String message) {
		super(StatusCode.HTTP_491_BAD_CUSTOMER_REQUEST, message);
	}

	public BadCustomerRequestException(Throwable cause) {
		super(StatusCode.HTTP_491_BAD_CUSTOMER_REQUEST, cause);
	}

}
