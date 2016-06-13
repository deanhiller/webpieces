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
			throw new IllegalArgumentException("Invalid Route.  Cannot find 'public' method="+methodStr+" on class="+controllerStr);
		else if(matches.size() > 1) 
			throw new UnsupportedOperationException("You have more than one 'public' method named="+methodStr+" on class="+controllerStr+"  This is not yet supported until we support method parameters(let us know you hit this and we will immediately implement)");
		
		Method controllerMethod = matches.get(0);
		Parameter[] parameters = controllerMethod.getParameters();
		List<String> paramNames = new ArrayList<>();
		for(Parameter p : parameters) {
			
			String name = p.getName();
			Param annotation = p.getAnnotation(Param.class);
			if(annotation == null)
				throw new IllegalArgumentException("Method='"+controllerMethod+"' has to have every argument annotated with @Param(paramName) so we\n"
						+ "know what incoming parameter to map from.  NOTE: we may try to switch to variable names in the future but variable names are not always available");
			
			paramNames.add(annotation.value());
		}
		
		meta.setMethodParamNames(paramNames);
		meta.setControllerInstance(controllerInst);
		meta.setMethod(controllerMethod);		
	}

}
