package org.webpieces.router.api.ctx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.webpieces.router.api.dto.Cookie;

public class Flash extends FlashScope {

	private Map<String, List<String>> keysToValues = new HashMap<>();
	
	@Override
	protected Cookie toCookie() {
		
		return null;
	}

	public void saveFormParams(Map<String, List<String>> fields) {
		keysToValues.putAll(fields);
	}

}
