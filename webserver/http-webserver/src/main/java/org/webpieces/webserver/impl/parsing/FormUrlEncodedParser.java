package org.webpieces.webserver.impl.parsing;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.DataWrapper;

public class FormUrlEncodedParser implements BodyParser {

	@Override
	public void parse(DataWrapper body, RouterRequest routerRequest, Charset encoding) {
		String multiPartData = body.createStringFrom(0, body.getReadableSize(), encoding);
		Map<String, String> keyToValues = new HashMap<>();
		parse(multiPartData, (key, val) -> keyToValues.put(key, val));
		routerRequest.multiPartFields = keyToValues;
	}
	
	public void parse(String multiPartData, BiFunction<String, String, String> mapAddFunction) {
		try {
		    String[] pairs = multiPartData.split("\\&");
		    for (int i = 0; i < pairs.length; i++) {
		      String[] fields = pairs[i].split("=");
		      String name = URLDecoder.decode(fields[0], "UTF-8");
		      String value = null;
		      if(fields.length == 2)
		    	  value = URLDecoder.decode(fields[1], "UTF-8");
		      
		      mapAddFunction.apply(name, value);
		    }

		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
