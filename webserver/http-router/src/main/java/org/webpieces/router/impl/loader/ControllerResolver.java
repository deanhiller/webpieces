package org.webpieces.router.impl.loader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.webpieces.router.impl.model.RouteModuleInfo;
import org.webpieces.router.impl.routebldr.RouteInfo;
import org.webpieces.util.file.ClassUtil;

public class ControllerResolver {

	private Pattern pattern = Pattern.compile("[A-Za-z0-9_]\\.[A-Za-z0-9_]");
	
	//A webapp could override this class entirely to feed back locations of Class's and methods based on the 
	//Strings passed in from the RouteModules
	public ResolvedMethod resolveControllerClassAndMethod(RouteInfo base) {
		RouteModuleInfo moduleInfo = base.getRouteModuleInfo();
		String controllerAndMethod = base.getControllerMethodString();
		int index = controllerAndMethod.lastIndexOf(".");
		if(index < 0)
			throw new IllegalArgumentException("Your Controller.method='"+controllerAndMethod+"' forgot the . to separate controller and method");
		String methodStr = controllerAndMethod.substring(index+1);
		String controllerStr = controllerAndMethod.substring(0, index);
		if(countMatches(controllerAndMethod) > 1) {
			//If they do absolute class name like
			//org.webpieces.Controller.method, then just return it...
			ResolvedMethod method = new ResolvedMethod(controllerStr, methodStr);
			return method;			
		}
		
		if(controllerStr.startsWith("/")) {
			//absolute reference using /org/webpieces/Controller.method, just replace / with .
			controllerStr = controllerStr.replace("/", ".");
			controllerStr = controllerStr.substring(1);
		} else if(controllerStr.contains("/")) {
			//relative reference is annoying but easier for users..(and more concise..
			controllerStr = ClassUtil.translate(moduleInfo.packageName, controllerStr);
		} else {
			//finally for Controllers in the same package as the router, it makes the 
			//Controller.method really concise...
			controllerStr = moduleInfo.packageName+"."+controllerStr;
		}
		
		ResolvedMethod method = new ResolvedMethod(controllerStr, methodStr);
		return method;
	}

	private int countMatches(String controllerAndMethod) {
		Matcher matcher = pattern.matcher(controllerAndMethod);
		int counter = 0;
		while(matcher.find())
			counter++;
		return counter;
	}
	
}
