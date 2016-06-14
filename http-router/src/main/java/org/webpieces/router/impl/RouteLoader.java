package org.webpieces.router.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.HttpRouterConfig;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.actions.Redirect;
import org.webpieces.router.api.actions.Render;
import org.webpieces.router.api.dto.Request;
import org.webpieces.router.api.dto.Response;
import org.webpieces.router.api.routing.RouteId;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.RouterModules;
import org.webpieces.router.impl.loader.Loader;
import org.webpieces.router.impl.params.ArgumentTranslator;
import org.webpieces.util.file.VirtualFile;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class RouteLoader {
	private static final Logger log = LoggerFactory.getLogger(RouteLoader.class);
	
	private HttpRouterConfig config;
	private ArgumentTranslator argumentTranslator;
	protected RouterImpl router;

	@Inject
	public RouteLoader(HttpRouterConfig config, ArgumentTranslator argumentTranslator) {
		this.config = config;
		this.argumentTranslator = argumentTranslator;
	}
	
	public RouterModules load(Loader loader) {
		try {
			return loadImpl(loader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private RouterModules loadImpl(Loader loader) throws IOException {
		log.info("loading the master "+RouterModules.class.getSimpleName()+" class file");

		VirtualFile fileWithRouterModulesName = config.getRoutersFile();
		String moduleName;
		try (InputStream str = fileWithRouterModulesName.openInputStream()) {
			InputStreamReader reader = new InputStreamReader(str);
			BufferedReader bufReader = new BufferedReader(reader);
			moduleName = bufReader.readLine().trim();
		}

		log.info(RouterModules.class.getSimpleName()+" class to load="+moduleName);
		Class<?> clazz = loader.clazzForName(moduleName);
		Object obj = newInstance(clazz);
		if(!(obj instanceof RouterModules))
			throw new IllegalArgumentException("name="+moduleName+" does not implement "+RouterModules.class.getSimpleName());

		log.info(RouterModules.class.getSimpleName()+" loaded");

		RouterModules routerModule = (RouterModules) obj;
		Injector injector = createInjector(routerModule);

		addRoutes(routerModule, injector, loader);
		return routerModule;
	}

	//protected abstract void verifyRoutes(Collection<Route> allRoutes);

	public void addRoutes(RouterModules rm, Injector injector, Loader loader) {
		log.info("adding routes");
		
		router = new RouterImpl(new RouteInfo(), new ReverseRoutes(), loader, injector);
		
		for(RouteModule module : rm.getRouterModules()) {
			String packageName = module.getClass().getPackage().getName();
			RouterImpl.currentPackage = packageName;
			module.configure(router, packageName);
		}
		
		if(!router.getRouterInfo().isCatchallRouteSet())
			throw new IllegalStateException("Client RouterModule did not call top level router.setCatchAllRoute");
		
		log.info("added all routes to router");
	}
	
	private Injector createInjector(RouterModules rm) {
		List<Module> guiceModules = rm.getGuiceModules();
		
		Module module = Modules.combine(guiceModules);
		if(config.getOverridesModule() != null)
			module = Modules.override(module).with(config.getOverridesModule());
		
		Injector injector = Guice.createInjector(module);
		return injector;
	}
	
	private Object newInstance(Class<?> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
			throw new IllegalArgumentException("Your clazz="+clazz.getSimpleName()+" could not be created(are you missing default constructor? is it not public?)", e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Your clazz="+clazz.getSimpleName()+" could not be created", e);
		}
	}

	public MatchResult fetchRoute(Request req) {
		MatchResult meta = router.getRouterInfo().fetchRoute(req, req.relativePath);
		if(meta == null)
			throw new IllegalStateException("missing exception on creation if we go this far");

		return meta;
	}
	
	public void invokeRoute(MatchResult result, Request req, ResponseStreamer responseCb) {
		RouteMeta meta = result.getMeta();
		Object obj = meta.getControllerInstance();
		if(obj == null)
			throw new IllegalStateException("Someone screwed up, as controllerInstance should not be null at this point, bug");
		Method method = meta.getMethod();

		Object[] arguments = argumentTranslator.createArgs(result, req);
		
		CompletableFuture<Object> response = invokeMethod(obj, method, arguments);
		
		response.thenApply(o -> continueProcessing(req, method, o, responseCb))
			.exceptionally(e -> processException(responseCb, e));
	}

	public Object continueProcessing(Request r, Method method, Object response, ResponseStreamer responseCb) {
		if(response instanceof Redirect) {
			Redirect action = (Redirect) response;
			RouteId id = action.getId();
			ReverseRoutes reverseRoutes = router.getReverseRoutes();
			RouteMeta meta = reverseRoutes.get(id);
			
			if(meta == null)
				throw new IllegalArgumentException("Route="+id+" returned from method='"+method+"' was not added in the RouterModules");

			Route route = meta.getRoute();
			
			Map<String, String> keysToValues = formMap(route.getPathParamNames(), action.getArgs());
			
			Set<String> keySet = keysToValues.keySet();
			List<String> argNames = route.getPathParamNames();
			if(keySet.size() != argNames.size()) {
				throw new IllegalArgumentException("Method='"+method+"' returns a Redirect action with wrong number of arguments.  args="+keySet.size()+" when it should be size="+argNames.size());
			}

			String path = route.getPath();
			
			for(String name : argNames) {
				String value = keysToValues.get(name);
				if(value == null) 
					throw new IllegalArgumentException("Method='"+method+"' returns a Redirect that is missing argument key="+name+" to form the url on the redirect");
				path = path.replace("{"+name+"}", value);
			}
			
			Response httpResponse = new Response(null, r.domain, path);
			responseCb.sendRedirect(httpResponse);
		} else if(response instanceof Render) {
			//how are we going to render..
		}
		return null;
	}
	
	private Map<String, String> formMap(List<String> pathParamNames, List<Object> args) {
		if(pathParamNames.size() != args.size())
			throw new IllegalArgumentException("The Redirect object has the wrong number of arguments. args.size="+args.size()+" should be size="+pathParamNames.size());

		Map<String, String> nameToValue = new HashMap<>();
		for(int i = 0; i < pathParamNames.size(); i++) {
			String key = pathParamNames.get(i);
			Object obj = args.get(i);
			if(obj != null) {
				//TODO: need reverse binding here!!!!
				nameToValue.put(key, obj.toString());
			}
		}
		return nameToValue;
	}

	private Object processException(ResponseStreamer responseCb, Throwable e) {
		responseCb.failure(e);
		return null;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private CompletableFuture<Object> invokeMethod(Object obj, Method m, Object[] arguments) {
		try {
			Object retVal = m.invoke(obj, arguments);
			if(retVal instanceof CompletableFuture) {
				return (CompletableFuture) retVal;
			} else {
				return CompletableFuture.completedFuture(retVal);
			}
		} catch (Throwable e) {
			//return a completed future with the exception inside...
			CompletableFuture<Object> futExc = new CompletableFuture<Object>();
			futExc.completeExceptionally(e);
			return futExc;
		}
	}

	public void loadControllerIntoMetaObject(RouteMeta meta, boolean isInitializingAllControllers) {
		router.loadControllerIntoMetaObject(meta, isInitializingAllControllers);
	}
}
