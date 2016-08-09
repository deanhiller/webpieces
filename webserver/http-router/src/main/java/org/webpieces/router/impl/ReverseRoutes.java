package org.webpieces.router.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.webpieces.router.api.RouteNotFoundException;
import org.webpieces.router.api.routing.RouteId;

public class ReverseRoutes {

	//I don't like this solution(this class) all that much but it works for verifying routes in web pages exist with a run of
	//a special test to find web app errors before deploying it.  good enough beats perfect and lookup is still fast
	
	private Map<RouteId, RouteMeta> routeIdToRoute = new HashMap<>();
	
	private Map<String, RouteMeta> routeNameToRoute = new HashMap<>();
	private Set<String> duplicateNames = new HashSet<>();
	private Map<String, RouteMeta> classAndNameToRoute = new HashMap<>();
	private Set<String> duplicateClassAndNames = new HashSet<>();
	private Map<String, RouteMeta> fullClassAndNameToRoute = new HashMap<>();

	private Charset urlEncoding;
	
	public ReverseRoutes(Charset urlEncoding) {
		this.urlEncoding = urlEncoding;
	}
	
	public void addRoute(RouteId routeId, RouteMeta meta) {
		RouteMeta existingRoute = routeIdToRoute.get(routeId);
		if(existingRoute != null) {
			throw new IllegalStateException("You cannot use a RouteId twice.  routeId="+routeId
					+" first time="+existingRoute.getRoute().getPath()+" second time="+meta.getRoute().getPath());
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
	
	public RouteMeta get(RouteId id) {
		RouteMeta meta = routeIdToRoute.get(id);
		if(meta == null)
			throw new IllegalStateException("addRoute with a route id="+id+" was never called by your application, yet this controller is trying to use it");
		return meta;
	}

	public RouteMeta get(String name) {
		String[] pieces = name.split("\\.");
		if(pieces.length == 1)
			return getByName(name);
		else if(pieces.length == 2)
			return getByClassAndName(name);
		else if(pieces.length > 2) {
			return getByFullClassAndName(name);
		} else
			throw new IllegalStateException("bug, should never reach here");
	}

	private RouteMeta getByFullClassAndName(String name) {
		RouteMeta meta = fullClassAndNameToRoute.get(name);
		if(meta == null)
			throw new RouteNotFoundException("route="+name+" not found.");
		return meta;
	}

	private RouteMeta getByClassAndName(String name) {
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
		RouteMeta routeMeta = classAndNameToRoute.get(name);
		if(routeMeta == null)
			throw new RouteNotFoundException("route="+name+" not found");
		return routeMeta;
	}
	
	private RouteMeta getByName(String name) {
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
		RouteMeta routeMeta = routeNameToRoute.get(name);
		if(routeMeta == null)
			throw new RouteNotFoundException("route="+name+" not found.");
		return routeMeta;
	}
	
	@Override
	public String toString() {
		return "ReverseRoutes [routeIdToRoute=" + routeIdToRoute + "]";
	}

	public String convertToUrl(String routeId, Map<String, String> args) {
		RouteMeta routeMeta = get(routeId);
		Route route = routeMeta.getRoute();
		String path = route.getPath();
		List<String> pathParamNames = route.getPathParamNames();
		for(String param : pathParamNames) {
			String val = args.get(param);
			if(val == null)
				throw new RouteNotFoundException("missing argument.  param="+param+" is required to exist(and cannot be null as well).");
			String encodedVal = urlEncode(val);
			path = path.replace("{"+param+"}", encodedVal);
		}
		
		return path;
	}
	
	private String urlEncode(Object value) {
		try {
			return URLEncoder.encode(value.toString(), urlEncoding.name());
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
