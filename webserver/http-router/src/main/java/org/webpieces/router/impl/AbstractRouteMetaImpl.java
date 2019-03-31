package org.webpieces.router.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.impl.model.MatchResult2;

public abstract class AbstractRouteMetaImpl implements AbstractRouteMeta {

	private final BasicRoute route;
	private final Charset urlEncoding; //all routes that have url coming in.  ie. not NotFoundRoute nor InternalErrorRoute

	public AbstractRouteMetaImpl(BasicRoute route, Charset urCharset) {
		this.route = route;
		urlEncoding = urCharset;
	}
	
	public MatchResult2 matches2(RouterRequest request, String subPath) {
		Matcher matcher = route.matches(request, subPath);
		if(matcher == null)
			return new MatchResult2(false);
		else if(!matcher.matches())
			return new MatchResult2(false);

		List<String> names = route.getPathParamNames();
		Map<String, String> namesToValues = new HashMap<>();
		for(String name : names) {
			String value = matcher.group(name);
			if(value == null) 
				throw new IllegalArgumentException("Bug, something went wrong. request="+request);
			//convert special characters back to their normal form like '+' to ' ' (space)
			String decodedVal = urlDecode(value);
			namesToValues.put(name, decodedVal);
		}
		
		return new MatchResult2(namesToValues);
	}
	
	private String urlDecode(Object value) {
		try {
			return URLDecoder.decode(value.toString(), urlEncoding.name());
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String getLoggableString(String paddingElement) {
		boolean isHttpsOnly = route.getExposedPorts() == Port.HTTPS;
		String http = isHttpsOnly ? "https" : "http";
		return pad(route.getMethod(), 5, paddingElement)+":"+pad(http, 5, paddingElement)+" : "+route.getFullPath();	
	}
	
	private String pad(String msg, int n, String paddingElement) {
		int left = n-msg.length();
		if(left < 0)
			left = 0;
		
		for(int i = 0; i < left; i++) {
			msg += paddingElement;
		}
		return msg;
	}
	
}
