package org.webpieces.http.exception;

import java.util.List;

import org.webpieces.http.StatusCode;

public class BadRequestException extends HttpClientErrorException {

	private static final long serialVersionUID = 8725117695723001888L;
	private List<Violation> violations;

	public BadRequestException() {
		super(StatusCode.HTTP_400_BAD_REQUEST);
	}

	public BadRequestException(String message, Throwable cause) {
		super(StatusCode.HTTP_400_BAD_REQUEST, message, cause);
	}

	public BadRequestException(String message) {
		super(StatusCode.HTTP_400_BAD_REQUEST, message);
	}

	public BadRequestException(Throwable cause) {
		super(StatusCode.HTTP_400_BAD_REQUEST, cause);
	}

	public BadRequestException(List<Violation> violations) {
		super(StatusCode.HTTP_400_BAD_REQUEST);
		this.violations = violations;
	}

	public List<Violation> getViolations() {
		return violations;
	}
	
}
