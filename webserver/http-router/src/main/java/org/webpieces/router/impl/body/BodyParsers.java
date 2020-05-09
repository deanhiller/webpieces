package org.webpieces.router.impl.body;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class BodyParsers {

	private Map<String, BodyParser> typeToParser = new HashMap<>();
	
	@Inject
	public BodyParsers(FormUrlEncodedParser formParser) {
		typeToParser.put("application/x-www-form-urlencoded", formParser);
	}
	
	public BodyParser lookup(String typeHeader) {
		return typeToParser.get(typeHeader);
	}
}
