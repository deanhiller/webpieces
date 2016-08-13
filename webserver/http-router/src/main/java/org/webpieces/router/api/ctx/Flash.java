package org.webpieces.router.api.ctx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.webpieces.router.api.dto.RouterCookie;

public class Flash extends FlashScope {

	private Map<String, List<String>> keysToValues = new HashMap<>();
	
	public Flash(CookieFactory creator) {
		super(creator);
	}
	
	@Override
	protected RouterCookie toCookie() {
		return creator.createCookie(CookieFactory.COOKIE_NAME_PREFIX+"Flash", keysToValues);
	}

	public void saveFormParams(Map<String, List<String>> fields) {
		keysToValues.putAll(fields);
	}

}
