package org.webpieces.router.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.webpieces.ctx.api.CookieScope;
import org.webpieces.ctx.api.RouterCookie;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.RouterConfig;

public class CookieTranslator {

	//private static final Logger log = LoggerFactory.getLogger(CookieTranslator.class);
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
	
	public RouterCookie translateScopeToCookie(String name, Map<String, String> value, Integer maxAge) {
		try {
			return scopeToCookie(name, value, maxAge);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	private RouterCookie scopeToCookie(String name, Map<String, String> value, Integer maxAge) throws UnsupportedEncodingException {
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

	private StringBuilder translateValuesToCookieFormat(Map<String, String> value) throws UnsupportedEncodingException {
		StringBuilder data = new StringBuilder();
        String separator = "";
        for (Map.Entry<String, String> entry : value.entrySet()) {
        	String val = entry.getValue();
            if (val != null) {
    			String key = entry.getKey();
    			String encodedKey = URLEncoder.encode(key, config.getUrlEncoding().name());
    			String encodedVal = URLEncoder.encode(val, config.getUrlEncoding().name());
	                data.append(separator)
	                        .append(encodedKey)
	                        .append("=")
	                        .append(encodedVal);
	                separator = "&";
            }
        }
		return data;
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
		Map<String, String> dataMap = new HashMap<>();
		String value = routerCookie.value;
		String[] pieces = value.split("&");
		for(String piece : pieces) {
			String[] split = piece.split("=");
			if(split.length == 2) {
				String key = URLDecoder.decode(split[0], config.getUrlEncoding().name());
				String val = URLDecoder.decode(split[1], config.getUrlEncoding().name());
				dataMap.put(key, val);
			}
		}
		
		data.setMapData(dataMap);
		return data;
	}

}
