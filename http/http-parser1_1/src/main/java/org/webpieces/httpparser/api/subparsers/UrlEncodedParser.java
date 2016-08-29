package org.webpieces.httpparser.api.subparsers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.function.BiFunction;

public class UrlEncodedParser {
	
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
