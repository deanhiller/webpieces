package org.webpieces.http.exception;

public class Violation {

	private final String message;
	private final String path;

	public Violation(final String path, final String message) {
		this.path = path;
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public String getPath() {
		return path;
	}
	
}
