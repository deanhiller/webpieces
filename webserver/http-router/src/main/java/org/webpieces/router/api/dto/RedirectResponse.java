package org.webpieces.router.api.dto;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.router.api.actions.Redirect;

public class RedirectResponse implements Redirect {

	public Boolean isHttps;
	public String domain;
	public String redirectToPath;
	public List<RouterCookie> cookies = new ArrayList<>();

	public RedirectResponse(Boolean isHttps, String domain, String redirectToPath, List<RouterCookie> cookies) {
		this.isHttps = isHttps;
		this.domain = domain;
		this.redirectToPath = redirectToPath;
	}

	@Override
	public String toString() {
		return "Response [isHttps=" + isHttps + ", domain=" + domain + ", path=" + redirectToPath + "]";
	}
	
}
