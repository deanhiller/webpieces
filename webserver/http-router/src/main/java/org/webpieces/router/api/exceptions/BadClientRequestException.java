package org.webpieces.router.api.exceptions;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

import com.webpieces.http2.api.dto.lowlevel.StatusCode;

public class BadClientRequestException extends HttpException {

	private static final long serialVersionUID = 8725117695723001888L;
	private List<Violation> violations;

	public BadClientRequestException() {
		super(StatusCode.HTTP_400_BADREQUEST);
	}

	public BadClientRequestException(String message, Throwable cause) {
		super(StatusCode.HTTP_400_BADREQUEST, message, cause);
	}

	public BadClientRequestException(String message) {
		super(StatusCode.HTTP_400_BADREQUEST, message);
	}

	public BadClientRequestException(Throwable cause) {
		super(StatusCode.HTTP_400_BADREQUEST, cause);
	}

	public BadClientRequestException(List<Violation> violations) {
		super(StatusCode.HTTP_400_BADREQUEST);
		this.violations = violations;
	}

	public List<Violation> getViolations() {
		return violations;
	}
	
}
