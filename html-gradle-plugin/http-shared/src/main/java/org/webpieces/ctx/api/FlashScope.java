package org.webpieces.ctx.api;

public interface FlashScope extends CookieScope {

	/**
	 * forces this cookie to be kept from now, through the response until the next request and controller invocation and then deleted (unless that controller calls keep)
	 * 
	 * 1. controller calls keep()
	 * 2. controller redirect or render
	 * 3. browser receives cookie
	 * 4. user does something to make browser do another request(POST or GET)
	 * 5. this scope is recreated since keep() was called and next controller has access to previous state from step #1
	 * 6. After you response this time, cookie is deleted UNLESS you call keep() again in this controller
	 * 
	 * ie. this cookie will last from controller invocation to next controller invocation each time keep is called.
	 * 
	 */
	@Deprecated
	void keep();
	
	void keep(boolean isKeep);
	
	/**
	 * Allows platform to see if you called noKeep(); or keep();
	 * @return
	 */
	boolean isKeepFlagSet();
	
	
}
