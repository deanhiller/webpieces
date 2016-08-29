package org.webpieces.webserver.impl.parsing;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.subparsers.UrlEncodedParser;

public class FormUrlEncodedParser implements BodyParser {

	private UrlEncodedParser parser;

	@Inject
	public FormUrlEncodedParser(UrlEncodedParser parser) {
		this.parser = parser;
	}
	
	@Override
	public void parse(DataWrapper body, RouterRequest routerRequest, Charset encoding) {
		String multiPartData = body.createStringFrom(0, body.getReadableSize(), encoding);
		Map<String, String> keyToValues = new HashMap<>();
		parser.parse(multiPartData, (key, val) -> keyToValues.put(key, val));
		routerRequest.multiPartFields = keyToValues;
	}
	

}
