package org.webpieces.router.api.ctx;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.dto.RouterCookie;

public class CookieFactory {

	public static String COOKIE_NAME_PREFIX = "webpieces";
	private static final Logger log = LoggerFactory.getLogger(CookieFactory.class);
	private RouterConfig config;
	
	@Inject
	public CookieFactory(RouterConfig config) {
		this.config = config;
		log.error("rename HttpRouterConfig to RouterConfig");
	}

	public RouterCookie createCookie(String name, Map<String, List<String>> value) {
		try {
			return createCookieImpl(name, value);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	private RouterCookie createCookieImpl(String name, Map<String, List<String>> value) throws UnsupportedEncodingException {
		RouterCookie cookie = new RouterCookie();
		cookie.name= name;
    	cookie.domain = null;
    	cookie.path = "/";
    	cookie.maxAgeSeconds = 0;
		cookie.isHttpOnly = config.getIsCookiesHttpOnly();
		cookie.isSecure = config.getIsCookiesSecure();
		
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
        cookie.value = data.toString();
		return cookie;
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

}
