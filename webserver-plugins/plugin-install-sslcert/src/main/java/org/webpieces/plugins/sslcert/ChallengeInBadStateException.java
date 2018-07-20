package org.webpieces.plugins.sslcert;

public class ChallengeInBadStateException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ChallengeInBadStateException(String message) {
		super(message);
	}

}
