package com.webpieces.hpack.impl.subparsers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.subparsers.AcceptType;
import com.webpieces.hpack.api.subparsers.HeaderItem;
import com.webpieces.hpack.api.subparsers.HeaderPriorityParser;
import com.webpieces.hpack.api.subparsers.ParsedContentType;
import com.webpieces.hpack.api.subparsers.ResponseCookie;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

public class HeaderPriorityParserImpl implements HeaderPriorityParser {
	
	private static final Logger log = LoggerFactory.getLogger(HeaderPriorityParserImpl.class);

	@Override
	public List<String> parseAcceptEncoding(Http2Headers req) {
		String encoding = req.getSingleHeaderValue(Http2HeaderName.ACCEPT_ENCODING);
		if(encoding == null)
			return new ArrayList<>();
		
		List<String> headerItems = parsePriorityItems(encoding, s -> s);
		return headerItems;
	}
	
	@Override
	public List<Locale> parseAcceptLangFromRequest(Http2Headers req) {
		String langHeader = req.getSingleHeaderValue(Http2HeaderName.ACCEPT_LANGUAGE);
		if(langHeader == null)
			return new ArrayList<>();
		
		List<Locale> headerItems = parsePriorityItems(langHeader, s -> parseItem(s));
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
		    if(item == null)
		    	continue;
		    
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
    public ParsedContentType parseContentType(Http2Headers req) {
		String cookieHeader = req.getSingleHeaderValue(Http2HeaderName.CONTENT_TYPE);
		if(cookieHeader == null) {
			return null;
		}

		String fullValue = cookieHeader;
		String mimeType = null;
		String charSet = null;
		String boundary = null;
		
		
		boolean isMimeType = true;
    	String[] split = cookieHeader.split(";");
    	for(String keyValPair : split) {
    		if(isMimeType) {
    			mimeType = keyValPair.trim();
    			isMimeType = false;
    			continue;
    		}
    		
	    	int index = keyValPair.indexOf("=");
	    	String name = keyValPair.substring(0, index).trim();
	    	String val = keyValPair.substring(index+1).trim();
    	    	
	    	if("charset".equalsIgnoreCase(name)) {
	    		charSet = val.trim();
	    	} else if("boundary".equalsIgnoreCase(name)) {
	    		boundary = val.trim();
	    	}
    	}
		return new ParsedContentType(mimeType, charSet, boundary, fullValue);
    }
	
	@Override
    public Map<String, String> parseCookiesFromRequest(Http2Headers req) {
		String cookieHeader = req.getSingleHeaderValue(Http2HeaderName.COOKIE);
		if(cookieHeader == null)
			return new HashMap<>();
		
    	String[] split = cookieHeader.split(";");
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
    public Http2Header createHeader(ResponseCookie cookie) {
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

    	return new Http2Header(Http2HeaderName.SET_COOKIE, headerVal);
    }

	@Override
	public List<AcceptType> parseAcceptFromRequest(Http2Headers req) {
		List<AcceptType> list = new ArrayList<>();
		String acceptVal = req.getSingleHeaderValue(Http2HeaderName.ACCEPT);
		if(acceptVal == null)
			return list;
		
		return parsePriorityItems(acceptVal, s -> parseAcceptSubitem(s));
	}
	
	private AcceptType parseAcceptSubitem(String subItem) {
		String[] pieces = subItem.trim().split("/");
		if(pieces.length != 2) {
			log.warn("subItem not valid since missing / item="+subItem+". we are skipping it");
			return null;
		}
		
		if("*".equals(pieces[0])) {
			if("*".equals(pieces[1])) {
				return new AcceptType();
			} else {
				log.warn("subItem not valid since missing */"+pieces[1]+" is not allowed in spec.  item="+subItem+". we are skipping it");
				return null;
			}
		} else if("*".equals(pieces[1])) {
			return new AcceptType(pieces[1]);
		}
		
		return new AcceptType(pieces[0], pieces[1]);
	}
}
