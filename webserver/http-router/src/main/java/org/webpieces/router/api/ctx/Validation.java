package org.webpieces.router.api.ctx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.webpieces.router.api.dto.Cookie;

public class Validation extends FlashScope {
	
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
	protected Cookie toCookie() {
		return null;
	}

}
