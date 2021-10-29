package com.webpieces.http2.api.dto.lowlevel.lib;

import java.util.HashMap;
import java.util.Map;


public enum Http2HeaderName {
	//Per RFC, these are the General Header types...
	CACHE_CONTROL("cache-control", HeaderType.GENERAL),
	CONNECTION("connection", HeaderType.GENERAL),
	DATE("date", HeaderType.GENERAL),
	PRAGMA("pragma", HeaderType.GENERAL),
	TRAILER("trailer", HeaderType.GENERAL),
	//This is NOT supported in http2 and should not be used in http2
	TRANSFER_ENCODING("transfer-encoding", HeaderType.GENERAL),
	UPGRADE("upgrade", HeaderType.GENERAL),
	VIA("via", HeaderType.GENERAL),
	WARNING("warning", HeaderType.GENERAL),

	//https://tools.ietf.org/id/draft-thomson-hybi-http-timeout-01.html
	KEEP_ALIVE("keep-alive", HeaderType.GENERAL),
	
	//Per RFC, these are the Request Header types
	ACCEPT("accept", HeaderType.REQUEST),
	ACCEPT_CHARSET("accept-charset", HeaderType.REQUEST),
	ACCEPT_ENCODING("accept-encoding", HeaderType.REQUEST),
	ACCEPT_LANGUAGE("accept-language", HeaderType.REQUEST),
	AUTHORIZATION("authorization", HeaderType.REQUEST),
	COOKIE("cookie", HeaderType.REQUEST),
	EXPECT("expect", HeaderType.REQUEST),
	FROM("from", HeaderType.REQUEST),
	IF_MATCH("if-match", HeaderType.REQUEST),
	IF_MODIFIED_SINCE("if-modified-since", HeaderType.REQUEST),
	IF_NONE_MATCH("if-none-match", HeaderType.REQUEST),
	IF_RANGE("if-range", HeaderType.REQUEST),
	IF_UNMODIFIED_SINCE("if-unmodified-since", HeaderType.REQUEST),
	MAX_FORWARDS("max-forwards", HeaderType.REQUEST),
	ORIGIN("origin", HeaderType.REQUEST),
	PROXY_AUTHORIZATION("proxy-authorization", HeaderType.REQUEST),
	RANGE("range", HeaderType.REQUEST),
	REFERER("referer", HeaderType.REQUEST),
	TE("te", HeaderType.REQUEST),
	USER_AGENT("user-agent", HeaderType.REQUEST),
	UPGRADE_INSECURE_REQUESTS("upgrade-insecure-requests", HeaderType.REQUEST),
	ACCESS_CONTROL_REQUEST_METHOD("Access-Control-Request-Method", HeaderType.REQUEST),
	ACCESS_CONTROL_REQUEST_HEADERS("Access-Control-Request-Headers", HeaderType.REQUEST),

	//Per RFC, these are the Response Header types
	ACCEPT_RANGES("accept-ranges", HeaderType.RESPONSE),
	AGE("age", HeaderType.RESPONSE),
	ETAG("etag", HeaderType.RESPONSE),
	LOCATION("location", HeaderType.RESPONSE),
	PROXY_AUTHENTICATE("proxy-authenticate", HeaderType.RESPONSE),
	RETRY_AFTER("retry-after", HeaderType.RESPONSE),
	SET_COOKIE("set-cookie", HeaderType.RESPONSE),
	SERVER("server", HeaderType.RESPONSE),
	VARY("vary", HeaderType.RESPONSE),
	WWW_AUTHENTICATE("www-authenticate", HeaderType.RESPONSE),
	ACCESS_CONTROL_ALLOW_METHODS("Access-Control-Allow-Methods", HeaderType.RESPONSE),
	ACCESS_CONTROL_ALLOW_HEADERS("Access-Control-Allow-Headers", HeaderType.RESPONSE),
	ACCESS_CONTROL_ALLOW_ORIGIN("Access-Control-Allow-Origin", HeaderType.RESPONSE),
	ACCESS_CONTROL_ALLOW_CREDENTIALS("Access-Control-Allow-Credentials", HeaderType.RESPONSE),
	ACCESS_CONTROL_EXPOSE_HEADERS("Access-Control-Expose-Headers", HeaderType.RESPONSE),
	ACCESS_CONTROL_MAX_AGE("Access-Control-Max-Age", HeaderType.RESPONSE),

	ALLOW("allow", HeaderType.ENTITY),         
	CONTENT_ENCODING("content-encoding", HeaderType.ENTITY),     
	CONTENT_LANGUAGE("content-language", HeaderType.ENTITY),      
	CONTENT_LENGTH("content-length", HeaderType.ENTITY),          
	CONTENT_LOCATION("content-location", HeaderType.ENTITY),      
	CONTENT_MD5("content-md5", HeaderType.ENTITY),             
	CONTENT_RANGE("content-range", HeaderType.ENTITY),  
	CONTENT_TYPE("content-type", HeaderType.ENTITY),  
	EXPIRES("expires", HeaderType.ENTITY),       
	LAST_MODIFIED("last-modified", HeaderType.ENTITY),

	STATUS(":status", HeaderType.RESPONSE),
	
	METHOD(":method", HeaderType.REQUEST),
	SCHEME(":scheme", HeaderType.REQUEST),
	PATH(":path", HeaderType.REQUEST), 
	AUTHORITY(":authority", HeaderType.REQUEST),
	
	X_REQUESTED_WITH("x-requested-with", HeaderType.REQUEST), 
	X_FORWARDED_PROTO("x-forwarded-proto", HeaderType.REQUEST),

	;

	private static Map<String, Http2HeaderName> lookup = new HashMap<>();
	
	static {
		for(Http2HeaderName name : Http2HeaderName.values()) {
			lookup.put(name.getHeaderName().toLowerCase(), name);
		}
	}
	
	private String name;
	private HeaderType type;
	
	Http2HeaderName(String name, HeaderType type) {
		this.name = name;
		this.type = type;
	}
	
	public String getHeaderName() {
		return name;
	}

	public HeaderType getHeaderType() {
		return type;
	}
	
	public static Http2HeaderName lookup(String name) {
		return lookup.get(name.toLowerCase());
	}

}
