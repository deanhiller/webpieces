package org.webpieces.router.impl.routers;

import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Pattern;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.impl.UrlPath;

public class MatchInfo {

	private final String fullPath;
	private final String subPath;
	private final Port exposedPort;
	private final HttpMethod httpMethod;
	private final Pattern patternToMatch;
	private final List<String> pathParamNames;
	private final Charset urlEncoding;
	
	public MatchInfo(UrlPath urlPath, Port exposedPort, HttpMethod httpMethod, Charset urlEncoding, Pattern patternToMatch, List<String> pathParamNames) {
		super();
		this.subPath = urlPath.getSubPath();
		this.fullPath = urlPath.getFullPath();
		this.exposedPort = exposedPort;
		this.httpMethod = httpMethod;
		this.urlEncoding = urlEncoding;
		this.patternToMatch = patternToMatch;
		this.pathParamNames = pathParamNames;
	}

	public String getFullPath() {
		return fullPath;
	}

	public String getSubPath() {
		return subPath;
	}

	public Port getExposedPorts() {
		return exposedPort;
	}

	public List<String> getPathParamNames() {
		return pathParamNames;
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
	
	public String getLoggableString(String paddingElement) {
		boolean isHttpsOnly = exposedPort == Port.HTTPS;
		String http = isHttpsOnly ? "https" : "http";
		return pad(httpMethod+"", 5, paddingElement)+":"+pad(http, 5, paddingElement)+" : "+fullPath;	
	}

	public String getLoggableHtml(boolean portMatches, boolean methodMatches, boolean pathMatches, String paddingElement) {
		boolean isHttpsOnly = exposedPort == Port.HTTPS;
		String http = isHttpsOnly ? ":https" : ":http";
		http = paddingElement+http;
		
		if(portMatches) {
			http = "<span style=\"color:red;\">"+http+"</span>";
		}
		
		String method = httpMethod+"";
		if(methodMatches) {
			method = "<span style=\"color:red;\">"+method+"</span>";
		}
		
		String thePath = fullPath;
		if(pathMatches) {
			thePath = "<span style=\"color:red;\">"+thePath+"</span>";
		}
		
		//pad if http, but not https
		if(!isHttpsOnly) {
			http = http+paddingElement;
		}
		
		if(httpMethod == HttpMethod.GET) {
			method = method+paddingElement;
		}
		
		
		
		return method+paddingElement+paddingElement+http+paddingElement+" : "+paddingElement+thePath;	
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

	public Pattern getPattern() {
		return patternToMatch;
	}

	public Charset getUrlEncoding() {
		return urlEncoding;
	}
}
