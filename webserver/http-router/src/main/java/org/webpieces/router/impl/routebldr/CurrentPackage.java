package org.webpieces.router.impl.routebldr;

import org.webpieces.router.impl.model.RouteModuleInfo;

public class CurrentPackage {

	public static ThreadLocal<RouteModuleInfo> currentPackage = new ThreadLocal<>();

	public static RouteModuleInfo get() {
		return currentPackage.get();
	}
	
	public static void set(RouteModuleInfo info) {
		currentPackage.set(info);
	}
}
