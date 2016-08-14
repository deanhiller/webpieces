package org.webpieces.ctx.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Validation extends FlashScope implements CookieData {
	
	private Map<String, List<String>> fieldErrors = new HashMap<>();

	public void addError(String name, String error) {
		List<String> list = fieldErrors.get(name);
		if(list == null) {
			list = new ArrayList<>();
			fieldErrors.put(name, list);
		}
		list.add(error);
	}	
	
	public boolean hasErrors() {
		if(fieldErrors.size() > 0)
			return true;
		return false;
	}

	@Override
	public boolean isNeedCreateCookie() {
		return true;
	}

	@Override
	public String getName() {
		return FlashScope.COOKIE_NAME_PREFIX+"Errors";
	}

	@Override
	public Map<String, List<String>> getMapData() {
		return fieldErrors;
	}
}
