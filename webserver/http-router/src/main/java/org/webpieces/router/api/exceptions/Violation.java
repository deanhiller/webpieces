package org.webpieces.router.api.exceptions;

public class Violation {

	private String message;
	private String path;

	public Violation(String path, String message) {
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
