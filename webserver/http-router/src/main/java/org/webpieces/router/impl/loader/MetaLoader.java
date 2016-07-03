package org.webpieces.router.impl.loader;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import org.webpieces.router.api.routing.Param;
import org.webpieces.router.impl.RouteMeta;

@Singleton
public class MetaLoader {

	public void loadInstIntoMeta(RouteMeta meta, Object controllerInst, String methodStr) {
		Method[] methods = controllerInst.getClass().getMethods();
		List<Method> matches = new ArrayList<>();
		for(Method m : methods) {
			if(m.getName().equals(methodStr))
				matches.add(m);
		}

		String controllerStr = controllerInst.getClass().getSimpleName();
		if(matches.size() == 0)
			throw new IllegalArgumentException("Invalid Route.  Cannot find 'public' method='"+methodStr+"' on class="+controllerStr);
		else if(matches.size() > 1) 
			throw new UnsupportedOperationException("You have more than one 'public' method named="+methodStr+" on class="+controllerStr+"  This is not yet supported until we support method parameters(let us know you hit this and we will immediately implement)");
		
		Method controllerMethod = matches.get(0);
		Parameter[] parameters = controllerMethod.getParameters();
		List<String> paramNames = new ArrayList<>();
		for(Parameter p : parameters) {
			
			String value;
			String name = p.getName();
			if(matchesBadName(name)) {
				Param annotation = p.getAnnotation(Param.class);
				if(annotation == null)
					throw new IllegalArgumentException("Method='"+controllerMethod+"' has to have every argument annotated with @Param(paramName) since\n"
						+ "you are not compiling with -parameters to enable the param names to be built into the *.class files.  Most likely, you "
						+ "changed the build.gradle we generated or switched to a different build system and did not enable this compiler option");
				value = annotation.value();
			} else {
				//use the param name in the method...
				value = name;
			}
			
			paramNames.add(value);
		}
		
		meta.setMethodParamNames(paramNames);
		meta.setControllerInstance(controllerInst);
		meta.setMethod(controllerMethod);		
	}

	/**
	 * Specifially checks for param names named 'arg{number}' which is a compiler generated name when you don't use the -parameter compiler option
	 * @param name
	 * @return
	 */
	private boolean matchesBadName(String name) {
		if(!name.startsWith("arg"))
			return false;
		
		String substring = name.substring(3);
		//shoudl be a number
		try {
			Integer.parseInt(substring);
		} catch(NumberFormatException e) {
			return false; //who named their param argxxxxx...maybe some words are named like that
		}
		return true;
	}

}
