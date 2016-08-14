package org.webpieces.httpparser.api.common;

public class RequestCookie {
	protected String name;
	protected String value;
    
    public static RequestCookie createCookie(Header header) {
    	RequestCookie cookie = new RequestCookie();
    	String value = header.getValue();
    	String[] split = value.trim().split(";");
    	String keyValPair = split[0];
    	//there are many = signs but the first one is the cookie name...the other are embedded key=value pairs
    	int index = keyValPair.indexOf("=");
    	cookie.name = keyValPair.substring(0, index);
    	cookie.value = keyValPair.substring(index+1);
    	return cookie;
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
