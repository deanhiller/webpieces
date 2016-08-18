package org.webpieces.httpparser.impl.subparsers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.common.ResponseCookie;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.subparsers.HeaderItem;
import org.webpieces.httpparser.api.subparsers.HeaderPriorityParser;

public class HeaderPriorityParserImpl implements HeaderPriorityParser {
	
	
	@Override
	public List<Locale> parseAcceptLangFromRequest(HttpRequest req) {
		Header langHeader = req.getHeaderLookupStruct().getHeader(KnownHeaderName.ACCEPT_LANGUAGE);
		if(langHeader == null)
			return new ArrayList<>();
		
		List<Locale> headerItems = parsePriorityItems(langHeader.getValue(), s -> parseItem(s));
		return headerItems;
	}
	
	private Locale parseItem(String subItem) {
		  //Parse the locale
	    Locale locale = null;
	    String[] l = subItem.split("_");
	    switch(l.length){
	        case 2: locale = new Locale(l[0], l[1]); break;
	        case 3: locale = new Locale(l[0], l[1], l[2]); break;
	        default: locale = new Locale(l[0]); break;
	    }
	    return locale;
	}
	
	@Override
	public <T> List<T> parsePriorityItems(String value, Function<String, T> parseFunction) {
		List<HeaderItem<T>> headerItems = new ArrayList<>();
		for (String str : value.split(",")){
		    String[] arr = str.trim().replace("-", "_").split(";");

		    T item = parseFunction.apply(arr[0]);
		    
		  //Parse the q-value
		    Double q = 1.0D;
		    for (String s : arr){
		        s = s.trim();
		        if (s.startsWith("q=")){
		            q = Double.parseDouble(s.substring(2).trim());
		            break;
		        }
		    }

		  //Print the Locale and associated q-value
		    System.out.println(q + " - " + arr[0] + "\t " + item);
		    HeaderItem<T> headerItem = new HeaderItem<>(q, item);
		    headerItems.add(headerItem);
		}
		
		Collections.sort(headerItems);
		
		List<T> orderedItems = new ArrayList<>();
		for(HeaderItem<T> item : headerItems) {
			orderedItems.add(item.getItem());
		}
		return orderedItems;
	}

	@Override
    public Map<String, String> parseCookiesFromRequest(HttpRequest req) {
		Header cookieHeader = req.getHeaderLookupStruct().getHeader(KnownHeaderName.COOKIE);
		if(cookieHeader == null)
			return new HashMap<>();
		
    	String value = cookieHeader.getValue();
    	String[] split = value.trim().split(";");
    	Map<String, String> map = new HashMap<>();
    	for(String keyValPair : split) {
	    	//there are many = signs but the first one is the cookie name...the other are embedded key=value pairs
	    	int index = keyValPair.indexOf("=");
	    	String name = keyValPair.substring(0, index).trim();
	    	String val = keyValPair.substring(index+1).trim();
	    	map.put(name, val);
    	}
		return map;
    }
	
    /**
     * From https://www.owasp.org/index.php/HttpOnly
     * 
     * Set-Cookie: <name>=<value>[; <Max-Age>=<age>]
     * [; expires=<date>][; domain=<domain_name>]
     * [; path=<some_path>][; secure][; HttpOnly]
     * 
     * and http://mrcoles.com/blog/cookies-max-age-vs-expires/
     * 
     * @param cookie
     * @return
     */
	@Override
    public Header createHeader(ResponseCookie cookie) {
    	String name = cookie.getName();
    	String value = cookie.getValue();
    	Integer maxAgeSeconds = cookie.getMaxAgeSeconds();
    	String domain = cookie.getDomain();
    	String path = cookie.getPath();
    	boolean isHttpOnly = cookie.isHttpOnly();
    	boolean isSecure = cookie.isSecure();
    	
    	String headerVal = "";
    	if(name != null)
    		headerVal = name +"=";
    	else if(value == null)
    		throw new IllegalArgumentException("value in cookie cannot be null");
    	headerVal += value;
    	
    	if(maxAgeSeconds != null)
    		headerVal += "; Max-Age="+maxAgeSeconds;
    	if(domain != null)
    		headerVal += "; domain="+domain;
    	if(path != null)
    		headerVal += "; path="+path;
    	if(isSecure)
    		headerVal += "; secure";
    	if(isHttpOnly)
    		headerVal += "; HttpOnly";

    	return new Header(KnownHeaderName.SET_COOKIE, headerVal);
    }
}
