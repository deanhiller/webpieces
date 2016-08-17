package org.webpieces.ctx.api;

import java.util.HashMap;
import java.util.Map;

public class Validation extends FlashScope implements CookieScope {
	
	public static String COOKIE_NAME = FlashScope.COOKIE_NAME_PREFIX+"Errors";
	
	private Map<String, String> fieldErrors = new HashMap<>();

	public void addError(String name, String error) {
		fieldErrors.put(name, error);
	}	
	
	public boolean hasErrors() {
		if(fieldErrors.size() > 0)
			return true;
		return false;
	}

	@Override
	public String getName() {
		return COOKIE_NAME;
	}

	@Override
	public Map<String, String> getMapData() {
		return fieldErrors;
	}

	@Override
	public void setMapData(Map<String, String> dataMap) {
		fieldErrors = dataMap;
	}

	public Object getError(String fieldName) {
		return fieldErrors.get(fieldName);
	}
}
