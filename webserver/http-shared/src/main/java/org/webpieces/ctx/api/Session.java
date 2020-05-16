package org.webpieces.ctx.api;

public interface Session extends CookieScope {
	
    String fetchSecureToken();

}
