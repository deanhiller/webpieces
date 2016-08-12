package org.webpieces.webserver.impl.parsing;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.router.api.dto.RouterRequest;

public class FormUrlEncodedParser implements BodyParser {

	@Override
	public void parse(DataWrapper body, RouterRequest routerRequest, Charset encoding) {
		try {
			parseImpl(body, routerRequest, encoding);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void parseImpl(DataWrapper body, RouterRequest routerRequest, Charset encoding) throws UnsupportedEncodingException {
		Map<String, List<String>> keyToValues = new HashMap<>();
		String multiPartData = body.createStringFrom(0, body.getReadableSize(), encoding);
		
	    String[] pairs = multiPartData.split("\\&");
	    for (int i = 0; i < pairs.length; i++) {
	      String[] fields = pairs[i].split("=");
	      String name = URLDecoder.decode(fields[0], "UTF-8");
	      String value = URLDecoder.decode(fields[1], "UTF-8");
	      addToMap(keyToValues, name, value);
	    }
	    
	    routerRequest.multiPartFields = keyToValues;
	}

	private void addToMap(Map<String, List<String>> keyToValues, String name, String value) {
		List<String> values = keyToValues.get(name);
		if(values == null) {
			values = new ArrayList<>();
			keyToValues.put(name, values);
		}
		values.add(value);
	}

}
