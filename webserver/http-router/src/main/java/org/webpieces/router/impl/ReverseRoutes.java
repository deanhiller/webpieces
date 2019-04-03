package org.webpieces.router.impl;

import java.io.UnsupportedEncodingException;
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
import org.webpieces.router.api.PortConfig;
import org.webpieces.router.api.PortConfigCallback;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.exceptions.RouteNotFoundException;
import org.webpieces.router.api.plugins.ReverseRouteLookup;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.RouteId;
import org.webpieces.router.impl.routers.HtmlRouter;

public class ReverseRoutes implements ReverseRouteLookup {

	//I don't like this solution(this class) all that much but it works for verifying routes in web pages exist with a run of
	//a special test to find web app errors before deploying it.  good enough beats perfect and lookup is still fast
	
	private Map<RouteId, HtmlRouter> routeIdToRoute = new HashMap<>();
	
	private Map<String, HtmlRouter> routeNameToRoute = new HashMap<>();
	private Set<String> duplicateNames = new HashSet<>();
	private Map<String, HtmlRouter> classAndNameToRoute = new HashMap<>();
	private Set<String> duplicateClassAndNames = new HashSet<>();
	private Map<String, HtmlRouter> fullClassAndNameToRoute = new HashMap<>();

	private Charset urlEncoding;
	private PortConfigCallback portConfigCallback;
	private volatile PortConfig ports;

	public ReverseRoutes(RouterConfig config) {
		this.portConfigCallback = config.getPortConfigCallback();
		this.urlEncoding = config.getUrlEncoding();		
	}

	public void addRoute(RouteId routeId, HtmlRouter meta) {
		HtmlRouter existingRoute = routeIdToRoute.get(routeId);
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
	
	public HtmlRouter get(RouteId id) {
		HtmlRouter meta = routeIdToRoute.get(id);
		if(meta == null)
			throw new IllegalStateException("addRoute method with param route id="+id+" was never called by your application(your RouteModule files), yet this controller is trying to use it");
		return meta;
	}

	public HtmlRouter get(String name) {
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

	private HtmlRouter getByFullClassAndName(String name) {
		HtmlRouter meta = fullClassAndNameToRoute.get(name);
		if(meta == null)
			throw new RouteNotFoundException("route="+name+" not found.");
		return meta;
	}

	private HtmlRouter getByClassAndName(String name) {
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
		HtmlRouter routeMeta = classAndNameToRoute.get(name);
		if(routeMeta == null)
			throw new RouteNotFoundException("route="+name+" not found");
		return routeMeta;
	}
	
	private HtmlRouter getByName(String name) {
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
		HtmlRouter routeMeta = routeNameToRoute.get(name);
		if(routeMeta == null)
			throw new RouteNotFoundException("route="+name+" not found.");
		return routeMeta;
	}
	
	@Override
	public String toString() {
		return "ReverseRoutes [routeIdToRoute=" + routeIdToRoute + "]";
	}

	public String convertToUrl(String routeId, Map<String, String> args, boolean isValidating) {		
		HtmlRouter routeMeta = get(routeId);
		String urlPath = routeMeta.getFullPath();
		List<String> pathParamNames = routeMeta.getPathParamNames();
		for(String param : pathParamNames) {
			String val = args.get(param);
			if(val == null) {
				String strArgs = "";
				for(Entry<String, String> entry : args.entrySet()) {
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

	private String createUrl(HtmlRouter routeMeta, String urlPath) {
		RequestContext ctx = Current.getContext();
		RouterRequest request = ctx.getRequest();
		
		boolean isHttpsOnly = routeMeta.getExposedPorts() == Port.HTTPS;
		if(!isHttpsOnly || request.isHttps)
			return urlPath;
		
		//we are rendering an http page with a link to https so need to do special magic
		String domain = request.domain;

		if(ports == null)
			ports = portConfigCallback.fetchPortConfig();

		int httpsPort = ports.getHttpsPort();
		return "https://"+domain+":"+httpsPort +urlPath;
	}
	
	private String urlEncode(Object value) {
		try {
			return URLEncoder.encode(value.toString(), urlEncoding.name());
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isGetRequest(RouteId routeId) {
		return get(routeId).getHttpMethod() == HttpMethod.GET;
	}

	@Override
	public String convertToUrl(RouteId routeId) {
		HtmlRouter routeMeta = get(routeId);
		String urlPath = routeMeta.getFullPath();
		return createUrl(routeMeta, urlPath);
	}
}
