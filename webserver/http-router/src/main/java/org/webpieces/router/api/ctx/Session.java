package org.webpieces.router.api.ctx;

import java.util.List;

import org.webpieces.router.api.dto.RouterCookie;

public class Session {

	private CookieFactory cookieCreator;

	public Session(CookieFactory cookieCreator) {
		this.cookieCreator = cookieCreator;
	}
	
	public void addSelfAsCookie(List<RouterCookie> cookies) {
		RouterCookie cookie = cookieCreator.createCookie(CookieFactory.COOKIE_NAME_PREFIX+"Session", null); 
		cookies.add(cookie);
	}
	
}
