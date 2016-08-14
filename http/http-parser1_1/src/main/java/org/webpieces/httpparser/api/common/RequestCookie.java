package org.webpieces.httpparser.api.common;

import java.util.HashMap;
import java.util.Map;

public class RequestCookie {
	protected String name;
	protected String value;
    
    public static Map<String, RequestCookie> createCookies(Header header) {
    	String value = header.getValue();
    	String[] split = value.trim().split(";");
    	Map<String, RequestCookie> map = new HashMap<>();
    	for(String keyValPair : split) {
        	RequestCookie cookie = new RequestCookie();
	    	//there are many = signs but the first one is the cookie name...the other are embedded key=value pairs
	    	int index = keyValPair.indexOf("=");
	    	cookie.name = keyValPair.substring(0, index).trim();
	    	cookie.value = keyValPair.substring(index+1).trim();
	    	map.put(cookie.name, cookie);
    	}
		return map;
    }
    

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
