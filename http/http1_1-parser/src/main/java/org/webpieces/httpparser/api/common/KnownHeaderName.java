package org.webpieces.httpparser.api.common;

import java.util.HashMap;
import java.util.Map;

public enum KnownHeaderName {
	//Per RFC, these are the General Header types...
	CACHE_CONTROL("Cache-Control", HeaderType.GENERAL),
	CONNECTION("Connection", HeaderType.GENERAL),
	DATE("Date", HeaderType.GENERAL),
	PRAGMA("Pragma", HeaderType.GENERAL),
	TRAILER("Trailer", HeaderType.GENERAL),
	TRANSFER_ENCODING("Transfer-Encoding", HeaderType.GENERAL),
	UPGRADE("Upgrade", HeaderType.GENERAL),
	VIA("Via", HeaderType.GENERAL),
	WARNING("Warning", HeaderType.GENERAL),

	//https://tools.ietf.org/id/draft-thomson-hybi-http-timeout-01.html
	KEEP_ALIVE("Keep-Alive", HeaderType.GENERAL),
	
	//Per RFC, these are the Request Header types
	ACCEPT("Accept", HeaderType.REQUEST),
	ACCEPT_CHARSET("Accept-Charset", HeaderType.REQUEST),
	ACCEPT_ENCODING("Accept-Encoding", HeaderType.REQUEST),
	ACCEPT_LANGUAGE("Accept-Language", HeaderType.REQUEST),
	AUTHORIZATION("Authorization", HeaderType.REQUEST),
	COOKIE("Cookie", HeaderType.REQUEST),
	EXPECT("Expect", HeaderType.REQUEST),
	FROM("From", HeaderType.REQUEST),
	HOST("Host", HeaderType.REQUEST),
	IF_MATCH("If-Match", HeaderType.REQUEST),
	IF_MODIFIED_SINCE("If-Modified-Since", HeaderType.REQUEST),
	IF_NONE_MATCH("If-None-Match", HeaderType.REQUEST),
	IF_RANGE("If-Range", HeaderType.REQUEST),
	IF_UNMODIFIED_SINCE("If-Unmodified-Since", HeaderType.REQUEST),
	MAX_FORWARDS("Max-Forwards", HeaderType.REQUEST),
	ORIGIN("Origin", HeaderType.REQUEST),
	PROXY_AUTHORIZATION("Proxy-Authorization", HeaderType.REQUEST),
	RANGE("Range", HeaderType.REQUEST),
	REFERER("Referer", HeaderType.REQUEST),
	TE("TE", HeaderType.REQUEST),
	USER_AGENT("User-Agent", HeaderType.REQUEST),
	UPGRADE_INSECURE_REQUESTS("Upgrade-Insecure-Requests", HeaderType.REQUEST),
	ACCESS_CONTROL_REQUEST_METHOD("Access-Control-Request-Method", HeaderType.REQUEST),
	ACCESS_CONTROL_REQUEST_HEADERS("Access-Control-Request-Headers", HeaderType.REQUEST),

	//Per RFC, these are the Response Header types
	ACCEPT_RANGES("Accept-Ranges", HeaderType.RESPONSE),
	AGE("Age", HeaderType.RESPONSE),
	ETAG("ETag", HeaderType.RESPONSE),
	LOCATION("Location", HeaderType.RESPONSE),
	PROXY_AUTHENTICATE("Proxy-Authenticate", HeaderType.RESPONSE),
	RETRY_AFTER("Retry-After", HeaderType.RESPONSE),
	SET_COOKIE("Set-Cookie", HeaderType.RESPONSE),
	SERVER("Server", HeaderType.RESPONSE),
	VARY("Vary", HeaderType.RESPONSE),
	WWW_AUTHENTICATE("WWW-Authenticate", HeaderType.RESPONSE),
	ACCESS_CONTROL_ALLOW_METHODS("Access-Control-Allow-Methods", HeaderType.RESPONSE),
	ACCESS_CONTROL_ALLOW_HEADERS("Access-Control-Allow-Headers", HeaderType.RESPONSE),
	ACCESS_CONTROL_ALLOW_ORIGIN("Access-Control-Allow-Origin", HeaderType.RESPONSE),

	ALLOW("Allow", HeaderType.ENTITY),         
	CONTENT_ENCODING("Content-Encoding", HeaderType.ENTITY),     
	CONTENT_LANGUAGE("Content-Language", HeaderType.ENTITY),      
	CONTENT_LENGTH("Content-Length", HeaderType.ENTITY),          
	CONTENT_LOCATION("Content-Location", HeaderType.ENTITY),      
	CONTENT_MD5("Content-MD5", HeaderType.ENTITY),             
	CONTENT_RANGE("Content-Range", HeaderType.ENTITY),  
	CONTENT_TYPE("Content-Type", HeaderType.ENTITY),  
	EXPIRES("Expires", HeaderType.ENTITY),       
	LAST_MODIFIED("Last-Modified", HeaderType.ENTITY),
	// For HTTP2 upgrade
	HTTP2_SETTINGS("HTTP2-Settings", HeaderType.REQUEST),
	X_REQUESTED_WITH("X-Requested-With", HeaderType.REQUEST),
	;

	private static Map<String, KnownHeaderName> lookup = new HashMap<>();
	
	static {
		for(KnownHeaderName name : KnownHeaderName.values()) {
			lookup.put(name.getHeaderName().toLowerCase(), name);
		}
	}
	
	private String name;
	private HeaderType type;
	
	KnownHeaderName(String name, HeaderType type) {
		this.name = name;
		this.type = type;
	}
	
	public String getHeaderName() {
		return name;
	}

	public HeaderType getHeaderType() {
		return type;
	}
	
	public static KnownHeaderName lookup(String name) {
		return lookup.get(name.toLowerCase());
	}
}
