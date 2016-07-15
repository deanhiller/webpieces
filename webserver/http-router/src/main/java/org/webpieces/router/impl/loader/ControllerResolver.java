package org.webpieces.router.impl.loader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.webpieces.router.impl.Route;
import org.webpieces.router.impl.RouteMeta;

public class ControllerResolver {

	private Pattern pattern = Pattern.compile("[A-Za-z0-9_]\\.[A-Za-z0-9_]");
	
	//A webapp could override this class entirely to feed back locations of Class's and methods based on the 
	//Strings passed in from the RouteModules
	public ResolvedMethod resolveControllerClassAndMethod(RouteMeta meta) {
		Route r = meta.getRoute();
		String controllerAndMethod = r.getControllerMethodString();
		int index = controllerAndMethod.lastIndexOf(".");
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
			controllerStr = translate(meta.getPackageContext(), controllerStr);
		} else {
			//finally for Controllers in the same package as the router, it makes the 
			//Controller.method really concise...
			controllerStr = meta.getPackageContext()+"."+controllerStr;
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

	private String translate(String packageContext, String controllerStr) {
		String[] split = controllerStr.split("/");
		for(int i = 0; i < split.length-1; i++) {
			String partialPath = split[i];
			if("..".equals(partialPath)) {
				packageContext = stripOneOff(packageContext);
			} else {
				packageContext = append(packageContext, partialPath);
			}
		}
		
		return packageContext + "." + split[split.length-1];
	}

	private String append(String packageContext, String partialPath) {
		if("".equals(packageContext))
			return partialPath;
		
		return packageContext + "." + partialPath;
	}

	private String stripOneOff(String packageContext) {
		int lastIndexOf = packageContext.lastIndexOf(".");
		if(lastIndexOf > 0) {
			return packageContext.substring(0, lastIndexOf);
		} else if(!"".equals(packageContext)) {
			return "";
		} else 
			throw new IllegalArgumentException("Too many .. were used in the path and we ended up going past the root classpath");
	}
	
}
