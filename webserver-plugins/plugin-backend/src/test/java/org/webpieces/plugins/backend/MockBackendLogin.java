package org.webpieces.plugins.backend;

import org.webpieces.plugins.backend.login.BackendLogin;

public class MockBackendLogin implements BackendLogin {

	private boolean isAuthenticatePass;

	@Override
	public boolean isLoginValid(String username, String password) {
		return isAuthenticatePass;
	}

	public void setAuthentication(boolean isAuthenticatePass) {
		this.isAuthenticatePass = isAuthenticatePass;
	}

}
