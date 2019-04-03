package org.webpieces.router.impl.routing;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.RouteId;
import org.webpieces.router.impl.BaseRouteInfo;
import org.webpieces.router.impl.RegExResult;
import org.webpieces.router.impl.RegExUtil;
import org.webpieces.router.impl.RouteInvoker2;
import org.webpieces.router.impl.UrlPath;
import org.webpieces.router.impl.loader.svc.RouteData;
import org.webpieces.router.impl.loader.svc.RouteInfoForHtml;
import org.webpieces.router.impl.model.MatchResult2;

public class HtmlRouter implements AbstractRouter {
	
	private final RouteInvoker2 invoker;

	private final String fullPath;
	private final Port exposedPort;
	private final HttpMethod httpMethod;
	private final RouteId routeId;
	private final boolean isCheckSecureToken;
	private final Pattern patternToMatch;
	private final List<String> argNames;
	private final Charset urlEncoding;
	
	//hmmmm, this was a bit of a pain.  It is only set once but it's hard to design the code to pass in during construction
	//without quite a bit of work
	private BaseRouteInfo baseRouteInfo;
	private DynamicInfo dynamicInfo;

	public HtmlRouter(RouteInvoker2 invoker, UrlPath urlPath, Port exposedPort, HttpMethod httpMethod, RouteId routeId,
			boolean checkToken, Charset urlEncoding) {
				this.invoker = invoker;
				
				this.urlEncoding = urlEncoding;
				this.fullPath = urlPath.getFullPath();
				this.exposedPort = exposedPort;
				this.httpMethod = httpMethod;
				this.routeId = routeId;
				this.isCheckSecureToken = checkToken;
				RegExResult result = RegExUtil.parsePath(urlPath.getSubPath());
				this.patternToMatch = Pattern.compile(result.regExToMatch);
				this.argNames = result.argNames;
	}

	@Override
	public String getFullPath() {
		return fullPath;
	}

	@Override
	public Port getExposedPorts() {
		return exposedPort;
	}

	public HttpMethod getHttpMethod() {
		return httpMethod;
	}
	
	public List<String> getPathParamNames() {
		return argNames;
	}
	
	public boolean matchesMethod(HttpMethod method) {
		if(this.httpMethod == method)
			return true;
		return false;
	}

	@Override
	public CompletableFuture<Void> invoke(RequestContext ctx, ResponseStreamer responseCb,
			Map<String, String> pathParams) {
		RouteData data = new RouteInfoForHtml(isCheckSecureToken);
		return invoker.invokeHtmlController(baseRouteInfo, dynamicInfo, ctx, responseCb, data);
	}
	
	
	
	public MatchResult2 matches2(RouterRequest request, String subPath) {
		Matcher matcher = matches(request, subPath);
		if(matcher == null)
			return new MatchResult2(false);
		else if(!matcher.matches())
			return new MatchResult2(false);

		List<String> names = getPathParamNames();
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
	
	public Matcher matches(RouterRequest request, String path) {
		if(exposedPort == Port.HTTPS && !request.isHttps) {
			//NOTE: we cannot do if isHttpsRoute != request.isHttps as every http route is 
			//allowed over https as well by default.  so 
			//isHttpsRoute=false and request.isHttps=true is allowed
			//isHttpsRoute=false and request.isHttps=false is allowed
			//isHttpsRoute=true  and request.isHttps=true is allowed
			return null; //route is https but request is http so not allowed
		} else if(this.httpMethod != request.method) {
			return null;
		}
		
		Matcher matcher = patternToMatch.matcher(path);
		return matcher;
	}

	public void setBaseRouteInfo(BaseRouteInfo baseRouteInfo) {
		this.baseRouteInfo = baseRouteInfo;
	}

	public void setDynamicInfo(DynamicInfo dynamicInfo) {
		this.dynamicInfo = dynamicInfo;
	}
}
