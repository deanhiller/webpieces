package org.webpieces.router.impl.loader;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.PathParam;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.util.filters.Service;

@Singleton
public class MetaLoader {

	@Inject
	public MetaLoader() {
	}

	public LoadedController loadInstIntoMeta(Object controllerInst, String methodStr) {
		Method[] methods = controllerInst.getClass().getMethods();
		List<Method> matches = new ArrayList<>();
		for(Method m : methods) {
			if(m.getName().equals(methodStr) && !m.isBridge())
				matches.add(m);
		}

		String controllerStr = controllerInst.getClass().getSimpleName();
		if(matches.size() == 0)
			throw new IllegalArgumentException("Invalid Route.  Cannot find 'public' method='"+methodStr+"' on class="+controllerStr);
		else if(matches.size() > 1) 
			throw new UnsupportedOperationException("You have more than one 'public' method named="+methodStr+" on class="+controllerStr+"  This is not yet supported until we support method parameters(let us know you hit this and we will immediately implement)");

		//NOTE: Leave this here, BUT we do not check this as you can send in a url like
		//       host.com/something?name=myname&secondparam=testing  rather than
		//       host.com/something/myname/testing with captures /something/{name}/{secondparam}
		//so basically, we could get the argNames of the captures here but this doesn't help us....
		//List<String> argNames = meta.getRoute().getArgNames();
		
		Method controllerMethod = matches.get(0);
		Parameter[] parameters = controllerMethod.getParameters();
		List<String> paramNames = new ArrayList<>();
		for(Parameter p : parameters) {
			
			String value;
			String name = p.getName();
			if(matchesBadName(name)) {
				PathParam annotation = p.getAnnotation(PathParam.class);
				if(annotation == null)
					throw new IllegalArgumentException("Method='"+controllerMethod+"' has to have every argument annotated with @PathParam(paramName) since\n"
						+ "you are not compiling with -parameters to enable the param names to be built into the *.class files.  Most likely, you "
						+ "changed the build.gradle we generated or switched to a different build system and did not enable this compiler option");
				value = annotation.value();
			} else {
				//use the param name in the method...
				value = name;
			}
			
			paramNames.add(value);
		}

		return new LoadedController(controllerInst, controllerMethod, parameters, paramNames);
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
			//who named their param arg###....I know it could have been args but not arg1Something and compiler generates arg0, arg1, etc.
			//so WE ASSUME this is compiler generated and it matched the badName then.  ie. arg0 or arg1 or arg2, etc.
			return false; 
		}
		return true;
	}

	public Service<MethodMeta, Action> loadFilters(Service<MethodMeta, Action> svc, List<RouteFilter<?>> filters) {
		for(RouteFilter<?> f : filters) {
			svc = ChainFilters.addOnTop(svc, f);
		}
		return svc;
	}

}
