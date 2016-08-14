package org.webpieces.ctx.api;

public abstract class FlashScope implements CookieData {

	protected boolean isKeep = false;
	public static String COOKIE_NAME_PREFIX = "webpieces";
	
	public void keep() {
		isKeep = true;
	}
	
	@Override
	public Integer getMaxAge() {
		Integer maxAgeSeconds = 0;
		if(isKeep)
			maxAgeSeconds = null;
		return maxAgeSeconds;
	}
}
