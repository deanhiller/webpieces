package org.webpieces.router.impl.routers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.impl.RegExResult;
import org.webpieces.router.impl.RegExUtil;
import org.webpieces.router.impl.UrlPath;
import org.webpieces.router.impl.model.MatchResult2;

public class MatchInfo {

	private final String fullPath;
	private final Port exposedPort;
	private final HttpMethod httpMethod;
	private final Pattern patternToMatch;
	private final List<String> argNames;
	private final Charset urlEncoding;
	
	public MatchInfo(UrlPath urlPath, Port exposedPort, HttpMethod httpMethod, Charset urlEncoding) {
		super();
		this.fullPath = urlPath.getFullPath();
		this.exposedPort = exposedPort;
		this.httpMethod = httpMethod;
		this.urlEncoding = urlEncoding;
		
		RegExResult result = RegExUtil.parsePath(urlPath.getSubPath());
		this.patternToMatch = Pattern.compile(result.regExToMatch);
		this.argNames = result.argNames;
	}
	
	public MatchResult2 matches2(RouterRequest request, String subPath) {
		Matcher matcher = matches(request, subPath);
		if(matcher == null)
			return new MatchResult2(false);
		else if(!matcher.matches())
			return new MatchResult2(false);

		Map<String, String> namesToValues = new HashMap<>();
		for(String name : argNames) {
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
	
	public Matcher matches(RouterRequest request, String path) {
		if(exposedPort == Port.HTTPS && !request.isHttps) {
			//NOTE: we cannot do if isHttpsRoute != request.isHttps as every http route is 
			//allowed over https as well by default.  so 
			//isHttpsRoute=false and request.isHttps=true is allowed
			//isHttpsRoute=false and request.isHttps=false is allowed
			//isHttpsRoute=true  and request.isHttps=true is allowed
			return null; //route is https but request is http so not allowed
		} else if(this.getHttpMethod() != request.method) {
			return null;
		}
		
		Matcher matcher = patternToMatch.matcher(path);
		return matcher;
	}

	public String getFullPath() {
		return fullPath;
	}

	public Port getExposedPorts() {
		return exposedPort;
	}

	public List<String> getPathParamNames() {
		return argNames;
	}

	public boolean matchesMethod(HttpMethod method) {
		if(this.getHttpMethod() == method)
			return true;
		return false;
	}

	public HttpMethod getHttpMethod() {
		return httpMethod;
	}
	
	@Override
	public String toString() {
		return "(port="+exposedPort+")"+httpMethod+" "+fullPath;
	}
}
