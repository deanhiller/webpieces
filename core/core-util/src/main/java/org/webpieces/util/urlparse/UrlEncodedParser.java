package org.webpieces.util.urlparse;

import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.util.net.URLEncoder;

/**
 * This is here as it is really independent of http1.1 and http2 parsers and we do not want
 * the router dependent on anyones http parser so it can be reused
 */
public class UrlEncodedParser {
	
	private static final Logger log = LoggerFactory.getLogger(UrlEncodedParser.class);
	
	public void parse(String multiPartData, BiFunction<String, String, Void> mapAddFunction) {
	    String[] pairs = multiPartData.split("\\&");
	    for (int i = 0; i < pairs.length; i++) {
	      String[] fields = pairs[i].split("=");
	      String name = URLEncoder.decode(fields[0], StandardCharsets.UTF_8);
	      
	      //Doing "" instead of null allows us to know which ones passed in a field name vs. what field names
	      //never existed.  if we do value=null, you have no way of knowing which is which
	      //ALSO, if you change this to null and login to https://localhost:8443/@backend with 
	      //username=admin and blank password, it will NullPointer so it makes it easier for webapp authors too
	      
	      //BUT this seems to break something else!!
	      String value = null;
	      if(fields.length == 2) {
	    	  try {
	    		  value = URLEncoder.decode(fields[1], StandardCharsets.UTF_8);
	    	  } catch(IllegalArgumentException e) {
	    		  //thrown when %3D is last piece of value BUT is chopped to %3D.  I don't like catching this
	    		  log.info("Some client fed a bad query param that can't be url decoded="+value);
	    		  value = fields[1]; //don't decode bad data so json request still works and it fails on lookup in DB or whatnot
	    	  }
	      }
	      
	      mapAddFunction.apply(name, value);
	    }
	}
}
