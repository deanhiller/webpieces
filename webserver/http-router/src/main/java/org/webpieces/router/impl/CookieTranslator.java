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
import org.webpieces.router.impl.ctx.CookieScopeImpl;

public class CookieTranslator {

	//private static final Logger log = LoggerFactory.getLogger(CookieTranslator.class);
	private RouterConfig config;
	
	@Inject
	public CookieTranslator(RouterConfig config) {
		this.config = config;
	}

	public void addScopeToCookieIfExist(List<RouterCookie> cookies, CookieScopeImpl data) {
		if(data.isNeedCreateSetCookie()) {
			RouterCookie cookie = translateScopeToCookie(data);
			cookies.add(cookie);
		} else if(data.isNeedCreateDeleteCookie()) {
			RouterCookie cookie = createBase(data.getName(), 0);
			cookies.add(cookie);
		}
	}
	
	public RouterCookie translateScopeToCookie(CookieScopeImpl data) {
		try {
			return scopeToCookie(data);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	private RouterCookie scopeToCookie(CookieScopeImpl scopeData) throws UnsupportedEncodingException {
		Map<String, String> mapData = scopeData.getMapData();
		RouterCookie cookie = createBase(scopeData.getName(), null);
		
		StringBuilder data = translateValuesToCookieFormat(mapData);
		cookie.value = data.toString();
		
		return cookie;
	}

	private RouterCookie createBase(String name, Integer maxAge) {
		RouterCookie cookie = new RouterCookie();
		cookie.name= name;
    	cookie.domain = null;
    	cookie.path = "/";
    	cookie.maxAgeSeconds = maxAge;
		cookie.isHttpOnly = config.getIsCookiesHttpOnly();
		cookie.isSecure = config.getIsCookiesSecure();
		cookie.value = "";
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

	public CookieScope translateCookieToScope(RouterRequest req, CookieScopeImpl data) {
		try {
			return cookieToScope(req, data);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	private CookieScope cookieToScope(RouterRequest req, CookieScopeImpl data) throws UnsupportedEncodingException {
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
