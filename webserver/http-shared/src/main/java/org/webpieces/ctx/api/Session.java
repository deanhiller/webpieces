package org.webpieces.ctx.api;

public interface Session extends CookieScope {
	
    public String getSecureToken();

}
