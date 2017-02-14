package org.webpieces.router.api.dto;

import org.webpieces.router.api.actions.Redirect;

public class RedirectResponse implements Redirect {

	public Boolean isHttps;
	public String domain;
	public int port;
	public String redirectToPath;

	public RedirectResponse(String redirectToPath) {
		this.redirectToPath = redirectToPath;
	}
	
	public RedirectResponse(Boolean isHttps, String domain, int port, String redirectToPath) {
		this.isHttps = isHttps;
		this.domain = domain;
		this.port = port;
		this.redirectToPath = redirectToPath;
		
	}

	@Override
	public String toString() {
		return "Response [isHttps=" + isHttps + ", domain=" + domain + ":" + port + ", path=" + redirectToPath + "]";
	}
	
}
