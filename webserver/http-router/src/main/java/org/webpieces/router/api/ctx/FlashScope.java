package org.webpieces.router.api.ctx;

import java.util.List;

import org.webpieces.router.api.dto.RouterCookie;

public abstract class FlashScope {

	protected boolean isKeep = false;
	protected CookieFactory creator;
	
	public FlashScope(CookieFactory creator) {
		this.creator = creator;
	}

	public void keep() {
		isKeep = true;
	}
	
	public void addSelfAsCookie(List<RouterCookie> cookies) {
		if(!isKeep)
			return;

		RouterCookie cookie = toCookie();
		cookies.add(cookie);
	}

	protected abstract RouterCookie toCookie();
}
