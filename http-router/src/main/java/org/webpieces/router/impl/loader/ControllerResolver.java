package org.webpieces.router.impl.loader;

import org.webpieces.router.impl.Route;
import org.webpieces.router.impl.RouteMeta;

public class ControllerResolver {

	//A webapp could override this class entirely to feed back locations of Class's and methods based on the 
	//Strings passed in from the RouteModules
	public ResolvedMethod resolveControllerClassAndMethod(RouteMeta meta) {
		Route r = meta.getRoute();
		String controllerAndMethod = r.getControllerMethodString();
		int lastIndex = controllerAndMethod.lastIndexOf(".");
		int fromBeginIndex = controllerAndMethod.indexOf(".");
		String methodStr = controllerAndMethod.substring(lastIndex+1);
		String controllerStr = controllerAndMethod.substring(0, lastIndex);
		if(lastIndex == fromBeginIndex) {
			controllerStr = meta.getPackageContext()+"."+controllerStr;
		}
		ResolvedMethod method = new ResolvedMethod(controllerStr, methodStr);
		return method;
	}
	
}
