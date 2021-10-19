package org.webpieces.ctx.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum HttpMethod {

	OPTIONS("OPTIONS"),
	GET("GET"),
	HEAD("HEAD"),
	PATCH("PATCH"),
	POST("POST"),
	PUT("PUT"),
	DELETE("DELETE"),
	TRACE("TRACE"),
	CONNECT("CONNECT");
	
	private static Map<String, HttpMethod> codeToKnownStatus = new HashMap<>();
	
	static {
		for(HttpMethod status : HttpMethod.values()) {
			codeToKnownStatus.put(status.getCode(), status);
		}
	}
	
	private String code;
	
	HttpMethod(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}

	public static HttpMethod lookup(String method) {
		if(method == null)
			return null;
		return codeToKnownStatus.get(method);
	}
	
	public static Set<HttpMethod> getAll() {
		Collection<HttpMethod> values = codeToKnownStatus.values();
		Set<HttpMethod> methods = new HashSet<>();
		methods.addAll(values);
		return methods;
	}
}
