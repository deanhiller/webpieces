package org.webpieces.plugins.sslcert;

import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.challenge.Http01Challenge;

public class AuthAndChallenge {

	private Authorization auth;
	private Http01Challenge challenge;

	public AuthAndChallenge(Authorization auth, Http01Challenge challenge) {
		this.auth = auth;
		this.challenge = challenge;
	}

	public Authorization getAuth() {
		return auth;
	}

	public Http01Challenge getChallenge() {
		return challenge;
	}
	
}
