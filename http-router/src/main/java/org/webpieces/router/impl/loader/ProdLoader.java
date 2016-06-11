package org.webpieces.router.impl.loader;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.impl.RouteMeta;

import com.google.inject.Injector;

@Singleton
public class ProdLoader implements Loader {

	private MetaLoader loader;
	
	@Inject
	public ProdLoader(MetaLoader loader) {
		this.loader = loader;
	}
	
	@Override
	public Class<?> clazzForName(String moduleName) {
		try {
			return Class.forName(moduleName);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Your clazz="+moduleName+" was not found on the classpath", e);
		}
	}
	
	private Object createController(Injector injector, String controllerClassFullName) {
		Class<?> clazz = clazzForName(controllerClassFullName);
		return injector.getInstance(clazz);
	}
	@Override
	public void loadControllerIntoMeta(RouteMeta meta, Injector injector, String controllerStr, String methodStr,
			boolean isInitializingAllControllers) {
		try {
			Object controllerInst = createController(injector, controllerStr);

			loader.loadInstIntoMeta(meta, controllerInst, methodStr);
		} catch(RuntimeException e) {
			String msg = "error=\n'"+e.getMessage()+"'\n"
					+"Check the stack trace for which client calls were calling addRoute or addXXXXRoute for which route is incorrect";
			throw new RuntimeException(msg, e);
		}
	}

}
