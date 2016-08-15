package org.webpieces.router.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.CookieScope;
import org.webpieces.ctx.api.RouterCookie;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.RouterConfig;

public class CookieTranslator {

	private static final Logger log = LoggerFactory.getLogger(CookieTranslator.class);
	private RouterConfig config;
	
	@Inject
	public CookieTranslator(RouterConfig config) {
		this.config = config;
	}

	public void addScopeToCookieIfExist(List<RouterCookie> cookies, CookieScope data) {
		if(data.isNeedCreateCookie()) {
			RouterCookie cookie = translateScopeToCookie(data.getName(), data.getMapData(), data.getMaxAge());
			cookies.add(cookie);
		}
	}
	
	public RouterCookie translateScopeToCookie(String name, Map<String, List<String>> value, Integer maxAge) {
		try {
			return scopeToCookie(name, value, maxAge);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	private RouterCookie scopeToCookie(String name, Map<String, List<String>> value, Integer maxAge) throws UnsupportedEncodingException {
		RouterCookie cookie = new RouterCookie();
		cookie.name= name;
    	cookie.domain = null;
    	cookie.path = "/";
    	cookie.maxAgeSeconds = maxAge;
		cookie.isHttpOnly = config.getIsCookiesHttpOnly();
		cookie.isSecure = config.getIsCookiesSecure();
		
		//do not send data on cookie delete
		if(maxAge != null && maxAge == 0) {
			cookie.value = "";
		} else {
			StringBuilder data = translateValuesToCookieFormat(value);
			cookie.value = data.toString();
		}
		return cookie;
	}

	private StringBuilder translateValuesToCookieFormat(Map<String, List<String>> value) throws UnsupportedEncodingException {
		StringBuilder data = new StringBuilder();
        String separator = "";
        for (Map.Entry<String, List<String>> entry : value.entrySet()) {
            if (entry.getValue() != null) {
    			String key = entry.getKey();
    			String encodedKey = URLEncoder.encode(key, config.getUrlEncoding().name());
    			String encodedVal = encodeValuePiece(entry);
            	if(encodedVal != null) {
	                data.append(separator)
	                        .append(encodedKey)
	                        .append("=")
	                        .append(encodedVal);
	                separator = "&";
            	}
            }
        }
		return data;
	}

	private String encodeValuePiece(Map.Entry<String, List<String>> entry) throws UnsupportedEncodingException {
		String encodedVal;
		List<String> valueList = entry.getValue();
		if(valueList.size() == 1) {
			String val = valueList.get(0);
			if(val == null)
				encodedVal = null;
			else
				encodedVal = URLEncoder.encode(val, config.getUrlEncoding().name());
		} else if(valueList.size() == 0) {
			throw new IllegalStateException("This should never be possible.  we never add an empty list(only list with size=1 with null element maybe which is ok)");
		} else {
			encodedVal = encodeList(valueList);
		}
		return encodedVal;
	}

	private String encodeList(List<String> valueList) {
		StringBuilder builder = new StringBuilder();
		String separator = "";
		for(String val : valueList) {
			if(val != null) {
				builder.append(separator)
						.append(val);
				separator = ",";
			}
		}
		if(builder.length() == 0)
			return null;
		return builder.toString();
	}

	public CookieScope translateCookieToScope(RouterRequest req, CookieScope data) {
		try {
			return cookieToScope(req, data);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	private CookieScope cookieToScope(RouterRequest req, CookieScope data) throws UnsupportedEncodingException {
		RouterCookie routerCookie = req.cookies.get(data.getName());
		if(routerCookie == null) {
			data.setExisted(false);
			return data;
		}
		
		data.setExisted(true);
		Map<String, List<String>> dataMap = new HashMap<>();
		String value = routerCookie.value;
		String[] pieces = value.split("&");
		for(String piece : pieces) {
			String[] split = piece.split("=");
			if(split.length == 2) {
				String key = URLDecoder.decode(split[0], config.getUrlEncoding().name());
				String val = URLDecoder.decode(split[1], config.getUrlEncoding().name());
				if(val.contains(",")) {
					String[] listElements = val.split(",");
					List<String> list = Arrays.asList(listElements);
					dataMap.put(key, list);
				} else {
					List<String> list = Arrays.asList(val);
					dataMap.put(key, list);
				}
			}
		}
		
		data.setMapData(dataMap);
		return data;
	}

}
