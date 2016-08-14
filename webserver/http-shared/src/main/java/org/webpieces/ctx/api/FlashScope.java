package org.webpieces.ctx.api;

public abstract class FlashScope implements CookieData {

	//4 cookie states....
	//did not exist, no need to create
	//did not exist, need to create (create cookie)
	//exist, need to keep (create cookie)
	//exist, need to delete (create delete cookie)
	protected boolean existed = false;
	protected boolean isKeep = false;
	public static String COOKIE_NAME_PREFIX = "webpieces";
	
	public void keep() {
		isKeep = true;
	}
	
	@Override
	public void setExisted(boolean existed) {
		this.existed = existed;
	}

	@Override
	public boolean isNeedCreateCookie() {
		if(!existed && !isKeep)
			return false;
		return true;
	}
	
	@Override
	public Integer getMaxAge() {
		Integer maxAgeSeconds = 0;
		if(isKeep)
			maxAgeSeconds = null;
		return maxAgeSeconds;
	}
}
