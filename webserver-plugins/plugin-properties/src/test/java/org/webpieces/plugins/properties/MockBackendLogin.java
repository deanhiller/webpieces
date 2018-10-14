package org.webpieces.plugins.properties;

import org.webpieces.plugins.backend.login.BackendLogin;

public class MockBackendLogin implements BackendLogin {

	@Override
	public boolean isLoginValid(String username, String password) {
		return false;
	}

}
