package org.webpieces.plugins.sslcert.acme;

import java.net.URL;
import java.time.Instant;

import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Http01Challenge;

public class ProxyAuthorization {

	private Authorization auth;

	public ProxyAuthorization(Authorization auth) {
		this.auth = auth;
	}

	public String getDomain() {
		return auth.getDomain();
	}

	public Instant getExpires() {
		return auth.getExpires();
	}

	public Status getStatus() {
		return auth.getStatus();
	}

	public URL getLocation() {
		return auth.getLocation();
	}

	public String getToken() {
		Http01Challenge challenge = auth.findChallenge(Http01Challenge.TYPE);
		return challenge.getToken();
	}
	
	public String getAuthContent() {
		Http01Challenge challenge = auth.findChallenge(Http01Challenge.TYPE);
		return challenge.getAuthorization();
	}	

}
