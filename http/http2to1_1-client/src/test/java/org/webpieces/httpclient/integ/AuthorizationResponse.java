package org.webpieces.httpclient.integ;

public class AuthorizationResponse {

	private boolean clientIdFound;
	private boolean authenticationSucceeded;
	private boolean authorizationSucceeded;
	private String message;

	public boolean isAuthenticationSucceeded() {
		return authenticationSucceeded;
	}

	public void setAuthenticationSucceeded(boolean authenticationSucceeded) {
		this.authenticationSucceeded = authenticationSucceeded;
	}

	public boolean isAuthorizationSucceeded() {
		return authorizationSucceeded;
	}

	public void setAuthorizationSucceeded(boolean authorizationSucceeded) {
		this.authorizationSucceeded = authorizationSucceeded;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setClientIdFound(boolean clientIdFound) {
		this.clientIdFound = clientIdFound;
	}

	public boolean isClientIdFound() {
		return clientIdFound;
	}

}
