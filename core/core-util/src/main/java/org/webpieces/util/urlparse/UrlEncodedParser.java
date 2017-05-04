package org.webpieces.util.urlparse;

import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;

import org.webpieces.util.net.URLEncoder;

/**
 * This is here as it is really independent of http1.1 and http2 parsers and we do not want
 * the router dependent on anyones http parser so it can be reused
 */
public class UrlEncodedParser {
	
	public void parse(String multiPartData, BiFunction<String, String, Void> mapAddFunction) {
	    String[] pairs = multiPartData.split("\\&");
	    for (int i = 0; i < pairs.length; i++) {
	      String[] fields = pairs[i].split("=");
	      String name = URLEncoder.decode(fields[0], StandardCharsets.UTF_8);
	      String value = null;
	      if(fields.length == 2)
	    	  value = URLEncoder.decode(fields[1], StandardCharsets.UTF_8);
	      
	      mapAddFunction.apply(name, value);
	    }
	}
}
