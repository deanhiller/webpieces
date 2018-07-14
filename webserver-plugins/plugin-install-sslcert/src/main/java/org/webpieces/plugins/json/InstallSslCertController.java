package org.webpieces.plugins.json;

import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Render;

public class InstallSslCertController {

	public Render sslSetup() {
		return Actions.renderThis();
	}

	public Render renderToken(String token) {
		return Actions.renderThis("token", token);
	}
	
}
