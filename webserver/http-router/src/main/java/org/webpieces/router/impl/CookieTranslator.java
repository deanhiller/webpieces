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
import org.webpieces.ctx.api.Value;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.exceptions.BadCookieException;
import org.webpieces.router.api.exceptions.CookieTooLargeException;
import org.webpieces.router.impl.ctx.CookieScopeImpl;
import org.webpieces.router.impl.ctx.SecureCookie;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.util.security.SecretKeyInfo;
import org.webpieces.util.security.Security;

public class CookieTranslator {

	private static final Logger log = LoggerFactory.getLogger(CookieTranslator.class);
	private static String VERSION = "1";
	//private static final Logger log = LoggerFactory.getLogger(CookieTranslator.class);
	private RouterConfig config;
	private Security security;
	
	@Inject
	public CookieTranslator(RouterConfig config, Security security) {
		this.config = config;
		this.security = security;
		if(config.getSecretKey() == null)
			throw new IllegalArgumentException("secret key must be set");
	}

	public void addScopeToCookieIfExist(List<RouterCookie> cookies, CookieScope cookie1) {
		if(!(cookie1 instanceof CookieScopeImpl))
			throw new IllegalArgumentException("Cookie is not the right data type="+cookie1.getClass()+" needs to be of type "+CookieScopeImpl.class);
		
		CookieScopeImpl data = (CookieScopeImpl) cookie1;
		if(data.isNeedCreateSetCookie()) {
			log.debug(()->"translating cookie="+cookie1.getName()+" to send to browser");
			RouterCookie cookie = translateScopeToCookie(data);
			cookies.add(cookie);
		} else if(data.isNeedCreateDeleteCookie()) {
			log.debug(()->"creating delete cookie for "+cookie1.getName()+" to send to browser");
			RouterCookie cookie = createDeleteCookie(data.getName());
			cookies.add(cookie);
		} else {
			log.debug(()->"not sending any cookie to browser for cookie="+cookie1.getName());
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
		Map<String, Value> mapData = scopeData.getMapData();
		RouterCookie cookie = createBase(scopeData.getName(), null);
		
		StringBuilder data = translateValuesToCookieFormat(mapData);
		
		String value = data.toString();
		if(scopeData instanceof SecureCookie) {
			SecretKeyInfo key = config.getSecretKey();
			String sign = security.sign(key, value);
			cookie.value = VERSION+"-"+sign+":"+value;
		} else {		
			cookie.value = VERSION+":"+value;
		}
		
		if(cookie.value.length() > 4050)
			throw new CookieTooLargeException("Your webserver has put too many things into the session cookie and"
					+ " browser will end up ignoring the cookie so we exception here to let you "
					+ "know.  Length of JUST the value(not whole cookie)="+cookie.value.length()+"\ncookie value="+cookie.value);
		
		return cookie;
	}

	public RouterCookie createDeleteCookie(String name) {
		return createBase(name, 0);
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

	private StringBuilder translateValuesToCookieFormat(Map<String, Value> value) throws UnsupportedEncodingException {
		StringBuilder data = new StringBuilder();
        String separator = "";
        for (Map.Entry<String, Value> entry : value.entrySet()) {
			String key = entry.getKey();
			Value holder = entry.getValue();
        	String val = holder.getValue();
			String encodedKey = URLEncoder.encode(key, config.getUrlEncoding().name());
            if (val != null) {
            	String encodedVal = URLEncoder.encode(val, config.getUrlEncoding().name());
                data.append(separator)
                    .append(encodedKey)
                    .append("=")
                    .append(encodedVal);
            } else {
            	//append just key if null.  must flash nulls or we would be resetting user changes in niche cases like clearing data or enums
                data.append(separator)
                    .append(encodedKey);
			}
            
            separator = "&";
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
		Map<String, Value> dataMap = new HashMap<>();
		String value = routerCookie.value;
		int colonIndex = value.indexOf(":");
		String version = value.substring(0, colonIndex);
		String keyValuePairs = value.substring(colonIndex+1);
		
		if(data instanceof SecureCookie) {
			String[] pair = version.split("-");
			version = pair[0];
			String expectedHash = pair[1];
			String hash = security.sign(config.getSecretKey(), keyValuePairs);
			if(!hash.equals(expectedHash))
				throw new BadCookieException("hashes don't match...This occurs if secret key"
						+ " was switched, or loaded different webapp on same port or someone"
						+ " created an invalid cookie and sent to your webserver", data.getName());
		}
		
		if(!VERSION.equals(version))
			throw new BadCookieException("versions don't match...This occurs if secret key"
						+ " was switched, or loaded different webapp on same port or someone"
						+ " created an invalid cookie and sent to your webserver", data.getName());
		
		String[] pieces = keyValuePairs.split("&");
		for(String piece : pieces) {
			String[] split = piece.split("=");
			if(split.length == 2) {
				String key = URLDecoder.decode(split[0], config.getUrlEncoding().name());
				String val = URLDecoder.decode(split[1], config.getUrlEncoding().name());
				dataMap.put(key, new Value(val));
			} else {
				String key = URLDecoder.decode(split[0], config.getUrlEncoding().name());
				dataMap.put(key, new Value(null));				
			}
		}
		
		data.setMapData(dataMap);
		return data;
	}

}
