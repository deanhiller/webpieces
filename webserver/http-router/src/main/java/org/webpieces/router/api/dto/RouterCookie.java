package org.webpieces.router.api.dto;

public class RouterCookie {
	public String name;
    public String value;
    
	public String path;
	public String domain;
	
	public boolean isSecure;
    public Integer maxAgeSeconds;
    public boolean isHttpOnly = true;
    
}
