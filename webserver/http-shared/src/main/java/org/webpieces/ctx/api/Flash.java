package org.webpieces.ctx.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Flash extends FlashScope implements CookieScope {

	public static String COOKIE_NAME = FlashScope.COOKIE_NAME_PREFIX+"Flash";
	
	private Map<String, String> keysToValues = new HashMap<>();
	
	void saveFormParams(Map<String, String> fields, Set<String> secureFieldNames) {
		for(Entry<String, String> entry : fields.entrySet()) {
			String key = entry.getKey();
			if(!secureFieldNames.contains(key))
				keysToValues.put(key, entry.getValue());
		}
	}

	@Override
	public String getName() {
		return COOKIE_NAME;
	}

	@Override
	public Map<String, String> getMapData() {
		return keysToValues;
	}

	@Override
	public void setMapData(Map<String, String> dataMap) {
		this.keysToValues = dataMap;
	}

	public String get(String name) {
		return keysToValues.get(name);
	}

}
