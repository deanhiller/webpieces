package org.webpieces.ctx.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Flash extends FlashScope implements CookieData {

	private static final Logger log = LoggerFactory.getLogger(Flash.class);
	private Map<String, List<String>> keysToValues = new HashMap<>();
	
	public void saveFormParams(Map<String, List<String>> fields) {
		keysToValues.putAll(fields);
	}

	@Override
	public boolean isNeedCreateCookie() {
		log.error("need to implement this so we only create when keep AND no previous cookie(ie. we don't need to create a delete cookie)");
		return true;
	}

	@Override
	public String getName() {
		return FlashScope.COOKIE_NAME_PREFIX+"Flash";
	}

	@Override
	public Map<String, List<String>> getMapData() {
		return keysToValues;
	}

}
