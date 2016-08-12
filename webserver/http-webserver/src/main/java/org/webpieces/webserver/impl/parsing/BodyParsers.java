package org.webpieces.webserver.impl.parsing;

import java.util.HashMap;
import java.util.Map;

public class BodyParsers {

	private static Map<String, BodyParser> typeToParser = new HashMap<>();
	
	static {
		typeToParser.put("application/x-www-form-urlencoded", new FormUrlEncodedParser());
	}
	
	public static BodyParser lookup(String typeHeader) {
		return typeToParser.get(typeHeader);
	}
}
