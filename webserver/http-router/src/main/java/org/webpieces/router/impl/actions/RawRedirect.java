package org.webpieces.router.impl.actions;

import org.webpieces.router.api.actions.Redirect;

public class RawRedirect implements Redirect {

	private String url;

	public RawRedirect(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

}
