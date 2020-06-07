package org.webpieces.plugin.sslcert;

import java.net.URL;
import java.time.Instant;

import org.shredzone.acme4j.Status;
import org.webpieces.plugin.secure.sslcert.acme.ProxyAuthorization;

public class MockProxyAuthorization extends ProxyAuthorization {

	private String domain;
	private Instant expires;
	private Status status;
	private URL location;
	private String token;
	private String authContent;

	public MockProxyAuthorization(String domain, Instant expires, Status status, URL location,
			String token, String authContent) {
		super(null);
		this.domain = domain;
		this.expires = expires;
		this.status = status;
		this.location = location;
		this.token = token;
		this.authContent = authContent;
	}
	
	public String getDomain() {
		return domain;
	}

	public Instant getExpires() {
		return expires;
	}

	public Status getStatus() {
		return status;
	}

	public URL getLocation() {
		return location;
	}

	public String getToken() {
		return token;
	}

	public String getAuthContent() {
		return authContent;
	}	
}
