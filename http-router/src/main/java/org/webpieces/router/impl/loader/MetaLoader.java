package org.webpieces.router.impl.loader;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

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
			throw new UnsupportedOperationException("You have more than one 'public' method named="+methodStr+" on class="+controllerStr+"  This is not yet supported until we support method parameters");
		
		Method method = matches.get(0);
		meta.setControllerInstance(controllerInst);
		meta.setMethod(method);		
	}

}
