package org.webpieces.httpparser.api.dto;

import java.util.HashMap;
import java.util.Map;

public enum KnownHttpMethod {

	OPTIONS("OPTIONS"),
	GET("GET"),
	HEAD("HEAD"),
	POST("POST"),
	PUT("PUT"),
	DELETE("DELETE"),
	TRACE("TRACE"),
	CONNECT("CONNECT")
	;
	
	private static Map<String, KnownHttpMethod> codeToKnownStatus = new HashMap<>();
	
	static {
		for(KnownHttpMethod status : KnownHttpMethod.values()) {
			codeToKnownStatus.put(status.getCode(), status);
		}
	}
	
	private String code;
	
	private KnownHttpMethod(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}

	public static KnownHttpMethod lookup(String method) {
		return codeToKnownStatus.get(method);
	}
}
