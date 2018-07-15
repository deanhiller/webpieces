package org.webpieces.plugins.sslcert;

import java.security.KeyPair;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.shredzone.acme4j.util.KeyPairUtils;
import org.webpieces.plugins.backend.MenuCreator;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Render;
import org.webpieces.router.api.routing.SimpleStorage;

@Singleton
public class InstallSslCertController {

	private SimpleStorage storage;
	private MenuCreator menuCreator;

	@Inject
	public InstallSslCertController(MenuCreator menuCreator, SimpleStorage storage) {
		this.menuCreator = menuCreator;
		this.storage = storage;
	}
	
	public Render sslSetup() {
		return Actions.renderThis("menus", menuCreator.getMenu());
	}

	public Render renderToken(String token) {
		KeyPair accountKeyPair = KeyPairUtils.createKeyPair(2048);
		
		return Actions.renderThis("token", token);
	}
	
}
