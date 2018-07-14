package org.webpieces.plugins.json;

import java.security.KeyPair;

import javax.inject.Inject;

import org.shredzone.acme4j.util.KeyPairUtils;
import org.webpieces.router.api.Startable;
import org.webpieces.router.api.routing.SimpleStorage;

public class InstallSslCertStartup implements Startable {

	private SimpleStorage storage;

	@Inject
	public InstallSslCertStartup(SimpleStorage storage) {
		this.storage = storage;
	}
	
	@Override
	public void start() {
		// TODO Auto-generated method stub
		
		//first detect if we already have a key pair!!!!
		
		
		KeyPair accountKeyPair = KeyPairUtils.createKeyPair(2048);
		
		//
	}

}
