package org.webpieces.util.futures;

import java.util.HashMap;
import java.util.Map;

public class FutureLocal {
	
	public final static ThreadLocal<Map<String, Object>> LOCAL = new ThreadLocal<>();

	public static void put(String key, Object value) {
		Map<String, Object> map = fetchMap();
		map.put(key, value);
	}	

	public static Object get(String key) {
		Map<String, Object> map = fetchMap();
		return map.get(key);
	}
	
	private static Map<String, Object> fetchMap() {
		Map<String, Object> map = LOCAL.get();
		if(map == null) {
			map = new HashMap<String, Object>();
			LOCAL.set(map);
		}
	
		return map;
	}
	
	
	protected static Map<String, Object> fetchState() {
		return fetchMap();
	}
	
	protected static void restoreState(Map<String, Object> map) {
		LOCAL.set(map);
	}
}
