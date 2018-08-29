package org.webpieces.router.api.dto;

public class RedirectResponse {

	public Boolean isHttps;
	public String domain;
	public int port;
	public String redirectToPath;
	public boolean isAjaxRedirect;

	public RedirectResponse(String redirectToPath) {
		this.redirectToPath = redirectToPath;
	}
	
	public RedirectResponse(boolean isAjaxRedirect, boolean isHttps, String domain, int port, String redirectToPath) {
		this.isAjaxRedirect = isAjaxRedirect;
		this.isHttps = isHttps;
		this.domain = domain;
		this.port = port;
		this.redirectToPath = redirectToPath;
		
	}

	@Override
	public String toString() {
		return "Response [isHttps=" + isHttps + ", domain=" + domain + ":" + port + ", path=" + redirectToPath + ", isAjaxRedirect="+isAjaxRedirect+"]";
	}
	
}
