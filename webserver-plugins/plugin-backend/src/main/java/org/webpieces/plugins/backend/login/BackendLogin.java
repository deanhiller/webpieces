package org.webpieces.plugins.backend.login;

public interface BackendLogin {

	boolean isLoginValid(String username, String password);
	
}
