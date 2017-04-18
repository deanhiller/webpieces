package org.webpieces.httpparser.api.subparsers;

import java.util.function.BiFunction;

import org.webpieces.util.net.URLEncoder;

public class UrlEncodedParser {
	
	public void parse(String multiPartData, BiFunction<String, String, Void> mapAddFunction) {
	    String[] pairs = multiPartData.split("\\&");
	    for (int i = 0; i < pairs.length; i++) {
	      String[] fields = pairs[i].split("=");
	      String name = URLEncoder.decode(fields[0]);
	      String value = null;
	      if(fields.length == 2)
	    	  value = URLEncoder.decode(fields[1]);
	      
	      mapAddFunction.apply(name, value);
	    }
	}
}
