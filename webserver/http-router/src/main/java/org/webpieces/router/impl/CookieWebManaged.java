package org.webpieces.router.impl;

public interface CookieWebManaged {

	public String getCategory();
	
	public boolean isCookiesHttpOnly();

	public void setCookiesHttpOnly(boolean isCookiesHttpOnly);

	public boolean isCookiesSecure();

	public void setCookiesSecure(boolean isCookiesSecure);
}
