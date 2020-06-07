package org.webpieces.plugin.properties;

import org.webpieces.plugin.backend.login.BackendLogin;

public class MockBackendLogin implements BackendLogin {

	@Override
	public boolean isLoginValid(String username, String password) {
		return false;
	}

}
