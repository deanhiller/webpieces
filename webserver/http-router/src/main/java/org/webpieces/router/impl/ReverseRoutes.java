package org.webpieces.router.impl;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.controller.actions.HttpPort;
import org.webpieces.router.api.exceptions.IllegalReturnValueException;
import org.webpieces.router.api.exceptions.RouteNotFoundException;
import org.webpieces.router.api.extensions.ObjectStringConverter;
import org.webpieces.router.api.plugins.ReverseRouteLookup;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.RouteId;
import org.webpieces.router.impl.params.ObjectToParamTranslator;
import org.webpieces.router.impl.params.ObjectTranslator;
import org.webpieces.router.impl.routeinvoker.PortAndIsSecure;
import org.webpieces.router.impl.routeinvoker.RedirectFormation;
import org.webpieces.router.impl.routers.MatchInfo;
import org.digitalforge.sneakythrow.SneakyThrow;

public class ReverseRoutes implements ReverseRouteLookup {

	//I don't like this solution(this class) all that much but it works for verifying routes in web pages exist with a run of
	//a special test to find web app errors before deploying it.  good enough beats perfect and lookup is still fast
	
	private Map<RouteId, ReversableRouter> routeIdToRoute = new HashMap<>();
	
	private Map<String, ReversableRouter> routeNameToRoute = new HashMap<>();
	private Set<String> duplicateNames = new HashSet<>();
	private Map<String, ReversableRouter> classAndNameToRoute = new HashMap<>();
	private Set<String> duplicateClassAndNames = new HashSet<>();
	private Map<String, ReversableRouter> fullClassAndNameToRoute = new HashMap<>();

	private Charset urlEncoding;

	private RedirectFormation redirectFormation;

	private ObjectTranslator translator;
	private ObjectToParamTranslator reverseTranslator;

	public ReverseRoutes(
		RouterConfig config, 
		RedirectFormation redirectFormation,
		ObjectTranslator translator,
		ObjectToParamTranslator reverseTranslator
	) {
		this.redirectFormation = redirectFormation;
		this.translator = translator;
		this.urlEncoding = config.getUrlEncoding();
		this.reverseTranslator = reverseTranslator;
	}

	public void addRoute(RouteId routeId, ReversableRouter meta) {
		ReversableRouter existingRoute = routeIdToRoute.get(routeId);
		if(existingRoute != null) {
			throw new IllegalStateException("You cannot use a RouteId twice.  routeId="+routeId
					+" first time="+existingRoute.getFullPath()+" second time="+meta.getFullPath());
		}
		
		routeIdToRoute.put(routeId, meta);
		
		String enumClassName = routeId.getClass().getSimpleName();
		String name = routeId.name();

		if(routeNameToRoute.containsKey(name)) {
			duplicateNames.add(name);
		}
		routeNameToRoute.put(name, meta);
		
		String classAndName = enumClassName+"."+name;
		if(classAndNameToRoute.containsKey(classAndName)) {
			duplicateClassAndNames.add(classAndName);
		}
		classAndNameToRoute.put(classAndName, meta);
		
		String fullClassAndName = routeId.getClass().getName() +"."+name;
		fullClassAndNameToRoute.put(fullClassAndName, meta);
	}

	public void finalSetup() {
		//remove duplicates from Map...
		for(String name : duplicateNames) {
			routeNameToRoute.remove(name);
		}
		for(String classAndName : duplicateClassAndNames) {
			classAndNameToRoute.remove(classAndName);
		}
	}
	
	public ReversableRouter get(RouteId id) {
		ReversableRouter meta = routeIdToRoute.get(id);
		if(meta == null)
			throw new IllegalStateException("addRoute method with param route id="+id+" was never called by your application(your RouteModule files), yet this controller is trying to use it");
		return meta;
	}

	public ReversableRouter get(String name) {
		String[] pieces = name.split("\\.");
		if(pieces.length == 1)
			return getByName(name);
		else if(pieces.length == 2)
			return getByClassAndName(name);
		else if(pieces.length > 2) {
			return getByFullClassAndName(name);
		} else
			throw new IllegalStateException("route not found='"+name+"'");
	}

	private ReversableRouter getByFullClassAndName(String name) {
		ReversableRouter meta = fullClassAndNameToRoute.get(name);
		if(meta == null)
			throw new RouteNotFoundException("route="+name+" not found.");
		return meta;
	}

	private ReversableRouter getByClassAndName(String name) {
		if(duplicateClassAndNames.contains(name)) {
			Set<RouteId> keySet = routeIdToRoute.keySet();
			String routes = "";
			for(RouteId id : keySet) {
				String potentialName = id.getClass().getSimpleName()+"."+id.name();
				if(name.equals(potentialName))
					routes += "\nroute="+id.getClass().getName()+"."+id.name();
			}
			
			throw new RouteNotFoundException("There is more than one route matching the class and name.  Qualify it with the package like org.web."
					+name+".  These are the conflicting ids which is why you need to be more specific="+routes);
		}
		ReversableRouter routeMeta = classAndNameToRoute.get(name);
		if(routeMeta == null)
			throw new RouteNotFoundException("route="+name+" not found");
		return routeMeta;
	}
	
	private ReversableRouter getByName(String name) {
		if(duplicateNames.contains(name)) {
			Set<RouteId> keySet = routeIdToRoute.keySet();
			String routes = "";
			for(RouteId id : keySet) {
				if(name.equals(id.name()))
					routes += "\nroute="+id.getClass();
			}
			
			throw new RouteNotFoundException("There is more than one route matching the name.  Qualify it with the class like XXXRouteId."
					+name+".  Same names are found in these enum classes="+routes);
		}
		ReversableRouter routeMeta = routeNameToRoute.get(name);
		if(routeMeta == null)
			throw new RouteNotFoundException("route="+name+" not found.");
		return routeMeta;
	}
	
	@Override
	public String toString() {
		return "ReverseRoutes [routeIdToRoute=" + routeIdToRoute + "]";
	}

	//for redirects
	public UrlInfo routeToUrl(RouteId routeId, Method method, Map<String, Object> args, RequestContext ctx, HttpPort requestedPort) {
		ReversableRouter routeMeta = get(routeId);
		if(routeMeta == null)
			throw new IllegalReturnValueException("Route="+routeId+" returned from method='"+method+"' was not added in the RouterModules");
				
		MatchInfo matchInfo = routeMeta.getMatchInfo();

		if(!matchInfo.matchesMethod(HttpMethod.GET))
			throw new IllegalReturnValueException("method='"+method+"' is trying to redirect to routeid="+routeId+" but that route is not a GET method route and must be");

		Map<String, String> keysToValues = reverseTranslator.formMap(method, matchInfo.getPathParamNames(), args);

		Set<String> keySet = keysToValues.keySet();
		List<String> argNames = matchInfo.getPathParamNames();
		if(keySet.size() != argNames.size()) {
			throw new IllegalReturnValueException("Method='"+method+"' returns a Redirect action with wrong number of arguments.  args="+keySet.size()+" when it should be size="+argNames.size());
		}

		String path = matchInfo.getFullPath();
		
		for(String name : argNames) {
			String value = keysToValues.get(name);
			if(value == null) 
				throw new IllegalArgumentException("Method='"+method+"' returns a Redirect that is missing argument key="+name+" to form the url on the redirect");
			path = path.replace("{"+name+"}", value);
		}

		PortAndIsSecure info = redirectFormation.calculateInfo(matchInfo, requestedPort, ctx.getRequest());
		boolean isSecure = info.isSecure();
		int port = info.getPort();
		
		return new UrlInfo(isSecure, port, path);
	}
	
	//for in the page
	public String routeToUrl(String routeId, Map<String, Object> args, boolean isValidating) {		
		ReversableRouter routeMeta = get(routeId);
		
		String urlPath = routeMeta.getFullPath();
		List<String> pathParamNames = routeMeta.getMatchInfo().getPathParamNames();
		for(String param : pathParamNames) {
			Object objVal = args.get(param);
			ObjectStringConverter<Object> objTranslator = translator.getConverterFor(objVal);
			String val = objTranslator.objectToString(objVal);
			if(val == null) {
				String strArgs = "";
				for(Entry<String, Object> entry : args.entrySet()) {
					boolean equals = entry.getKey().equals(param);
					strArgs = " ARG:'"+entry.getKey()+"'='"+entry.getValue()+"'   equals="+equals+"\n";
				}
				throw new RouteNotFoundException("missing argument.  param="+param+" is required"
						+ " to exist(and cannot be null as well).  route="+routeId+" args="+strArgs);
			}
			String encodedVal = urlEncode(val);
			urlPath = urlPath.replace("{"+param+"}", encodedVal);
		}
		
		if(isValidating)
			return urlPath;
		
		return createUrl(routeMeta, urlPath);
	}

	private String createUrl(ReversableRouter routeMeta, String urlPath) {
		RequestContext ctx = Current.getContext();
		RouterRequest request = ctx.getRequest();


		boolean isBoth = routeMeta.getMatchInfo().getExposedPorts() == Port.BOTH;
		//1. if route is 'not' https only (ie. BOTH), return url path as we can just use the relative urlPath and
		// stay on whatever the request is using in our redirect
		//2. OR if request is https, we can also just use the relative path since it will stay in https
		if(isBoth || request.isHttps)
			return urlPath;
		//else request is HTTP and route to go to is HTTPS so we have to do more work
		
		//we are rendering an http page with a link to https so need to do special magic
		String domain = request.domain;

		int httpsPort = redirectFormation.calculateHttpsPort(request);
		return "https://"+domain+":"+httpsPort +urlPath;
	}
	
	private String urlEncode(Object value) {
		try {
			return URLEncoder.encode(value.toString(), urlEncoding.name());
		} catch(UnsupportedEncodingException e) {
			throw SneakyThrow.sneak(e);
		}
	}

	@Override
	public boolean isGetRequest(RouteId routeId) {
		return get(routeId).getMatchInfo().methodMatches(HttpMethod.GET);
	}

	@Override
	public String convertToUrl(RouteId routeId) {
		ReversableRouter routeMeta = get(routeId);
		String urlPath = routeMeta.getFullPath();
		return createUrl(routeMeta, urlPath);
	}
}
