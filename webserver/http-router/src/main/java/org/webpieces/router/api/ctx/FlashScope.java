package org.webpieces.router.api.ctx;

import java.util.List;

import org.webpieces.router.api.dto.Cookie;

public abstract class FlashScope {

	protected boolean isKeep = false;
	
	public void keep() {
		isKeep = true;
	}
	
	public void addSelfAsCookie(List<Cookie> cookies) {
		if(!isKeep)
			return;

		Cookie cookie = toCookie();
		cookies.add(cookie);
	}

	protected abstract Cookie toCookie();
}
