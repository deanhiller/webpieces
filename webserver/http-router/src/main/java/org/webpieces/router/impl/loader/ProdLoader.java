package org.webpieces.router.impl.loader;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.hooks.ClassForName;
import org.webpieces.router.impl.hooks.MetaLoaderProxy;

import com.google.inject.Injector;

@Singleton
public class ProdLoader implements MetaLoaderProxy {

	private MetaLoader loader;
	private ClassForName classForName;
	
	@Inject
	public ProdLoader(MetaLoader loader, ProdClassForName classLoader) {
		this.loader = loader;
		this.classForName = classLoader;
	}
	
	private Object createController(Injector injector, String controllerClassFullName) {
		Class<?> clazz = classForName.clazzForName(controllerClassFullName);
		return injector.getInstance(clazz);
	}
	
	@Override
	public void loadControllerIntoMeta(RouteMeta meta, ResolvedMethod method,
			boolean isInitializingAllControllers) {
		try {
			String controllerStr = method.getControllerStr();
			String methodStr = method.getMethodStr();
			
			Injector injector = meta.getInjector();
			Object controllerInst = createController(injector, controllerStr);

			loader.loadInstIntoMeta(meta, controllerInst, methodStr);
		} catch(RuntimeException e) {
			String msg = "error=\n'"+e.getMessage()+"'\n"
					+"Check the stack trace for which client calls were calling addRoute or addXXXXRoute for which route is incorrect";
			throw new RuntimeException(msg, e);
		}
	}

}
