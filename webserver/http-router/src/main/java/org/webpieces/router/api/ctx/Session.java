package org.webpieces.router.api.ctx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.webpieces.router.api.dto.RouterCookie;

public class Session {

	private CookieFactory cookieCreator;

	public Session(CookieFactory cookieCreator) {
		this.cookieCreator = cookieCreator;
	}
	
	public void addSelfAsCookie(List<RouterCookie> cookies) {
		Map<String, List<String>> sessionData = new HashMap<>();
		RouterCookie cookie = cookieCreator.createCookie(CookieFactory.COOKIE_NAME_PREFIX+"Session", sessionData, null); 
		cookies.add(cookie);
	}
	
}
