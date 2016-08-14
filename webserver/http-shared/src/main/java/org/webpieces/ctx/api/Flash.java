package org.webpieces.ctx.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Flash extends FlashScope implements CookieScope {

	public static String COOKIE_NAME = FlashScope.COOKIE_NAME_PREFIX+"Flash";
	
	private Map<String, List<String>> keysToValues = new HashMap<>();
	
	void saveFormParams(Map<String, List<String>> fields, Set<String> secureFieldNames) {
		for(Entry<String, List<String>> entry : fields.entrySet()) {
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
	public Map<String, List<String>> getMapData() {
		return keysToValues;
	}

	@Override
	public void setMapData(Map<String, List<String>> dataMap) {
		this.keysToValues = dataMap;
	}

	public String get(String name) {
		List<String> list = keysToValues.get(name);
		if(list == null || list.size() == 0)
			return null;
		else if(list.size() == 1)
			return list.get(0);
		else
			throw new IllegalArgumentException("This flash element="+name+" is an array");
	}

}
