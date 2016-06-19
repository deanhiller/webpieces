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
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.HttpRouterConfig;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.actions.Redirect;
import org.webpieces.router.api.actions.RenderHtml;
import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.dto.RedirectResponse;
import org.webpieces.router.api.dto.RenderResponse;
import org.webpieces.router.api.dto.Request;
import org.webpieces.router.api.exceptions.IllegalReturnValueException;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.api.routing.RouteId;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.WebAppMetaInfo;
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
	protected RouterBuilder router;

	@Inject
	public RouteLoader(HttpRouterConfig config, ArgumentTranslator argumentTranslator) {
		this.config = config;
		this.argumentTranslator = argumentTranslator;
	}
	
	public WebAppMetaInfo load(Loader loader) {
		try {
			return loadImpl(loader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private WebAppMetaInfo loadImpl(Loader loader) throws IOException {
		log.info("loading the master "+WebAppMetaInfo.class.getSimpleName()+" class file");

		VirtualFile fileWithRouterModulesName = config.getRoutersFile();
		String moduleName;
		try (InputStream str = fileWithRouterModulesName.openInputStream()) {
			InputStreamReader reader = new InputStreamReader(str);
			BufferedReader bufReader = new BufferedReader(reader);
			moduleName = bufReader.readLine().trim();
		}

		log.info(WebAppMetaInfo.class.getSimpleName()+" class to load="+moduleName);
		Class<?> clazz = loader.clazzForName(moduleName);
		Object obj = newInstance(clazz);
		if(!(obj instanceof WebAppMetaInfo))
			throw new IllegalArgumentException("name="+moduleName+" does not implement "+WebAppMetaInfo.class.getSimpleName());

		log.info(WebAppMetaInfo.class.getSimpleName()+" loaded");

		WebAppMetaInfo routerModule = (WebAppMetaInfo) obj;
		Injector injector = createInjector(routerModule);

		loadAllRoutes(routerModule, injector, loader);
		return routerModule;
	}

	//protected abstract void verifyRoutes(Collection<Route> allRoutes);

	public void loadAllRoutes(WebAppMetaInfo rm, Injector injector, Loader loader) {
		log.info("adding routes");
		
		router = new RouterBuilder("", new RouteInfo(), new ReverseRoutes(), loader, injector);
		
		for(RouteModule module : rm.getRouterModules()) {
			String packageName = module.getClass().getPackage().getName();
			RouterBuilder.currentPackage = packageName;
			module.configure(router, packageName);
		}
		
		if(!router.getRouterInfo().isPageNotFoundRouteSet())
			throw new IllegalStateException("None of the RouteModule implementations called top level router.setNotFoundRoute.  Modules="+rm.getRouterModules());
		
		log.info("added all routes to router");
	}
	
	private Injector createInjector(WebAppMetaInfo rm) {
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
		RouteInfo routerInfo = router.getRouterInfo();
		MatchResult meta = routerInfo.fetchRoute(req, req.relativePath);
		if(meta == null)
			throw new IllegalStateException("missing exception on creation if we go this far");

		return meta;
	}
	
	public void invokeRoute(MatchResult result, Request req, ResponseStreamer responseCb, Supplier<MatchResult> notFoundRoute) {
		RouteMeta meta = result.getMeta();
		Object obj = meta.getControllerInstance();
		if(obj == null)
			throw new IllegalStateException("Someone screwed up, as controllerInstance should not be null at this point, bug");
		Method method = meta.getMethod();

		Object[] arguments;
		try {
			arguments = argumentTranslator.createArgs(result, req);
		} catch(NotFoundException e) {
			result = notFoundRoute.get();
			meta = result.getMeta();
			obj = meta.getControllerInstance();
			if(obj == null)
				throw new IllegalStateException("Someone screwed up, as controllerInstance should not be null at this point, bug");
			method = meta.getMethod();			
			arguments = argumentTranslator.createArgs(result, req);
		}
		
		CompletableFuture<Object> response = invokeMethod(obj, method, arguments);
		
		RouteMeta finalMeta = meta;
		response.thenApply(o -> continueProcessing(req, finalMeta, o, responseCb))
			.exceptionally(e -> processException(responseCb, e));
	}

	public Object continueProcessing(Request r, RouteMeta incomingRequestMeta, Object response, ResponseStreamer responseCb) {
		Method method = incomingRequestMeta.getMethod();
		if(response instanceof Redirect) {
			Redirect action = (Redirect) response;
			RouteId id = action.getId();
			ReverseRoutes reverseRoutes = router.getReverseRoutes();
			RouteMeta nextRequestMeta = reverseRoutes.get(id);
			
			if(nextRequestMeta == null)
				throw new IllegalReturnValueException("Route="+id+" returned from method='"+method+"' was not added in the RouterModules");

			Route route = nextRequestMeta.getRoute();
			
			Map<String, String> keysToValues = formMap(method, route.getPathParamNames(), action.getArgs());
			
			Set<String> keySet = keysToValues.keySet();
			List<String> argNames = route.getPathParamNames();
			if(keySet.size() != argNames.size()) {
				throw new IllegalReturnValueException("Method='"+method+"' returns a Redirect action with wrong number of arguments.  args="+keySet.size()+" when it should be size="+argNames.size());
			}

			String path = route.getPath();
			
			for(String name : argNames) {
				String value = keysToValues.get(name);
				if(value == null) 
					throw new IllegalArgumentException("Method='"+method+"' returns a Redirect that is missing argument key="+name+" to form the url on the redirect");
				path = path.replace("{"+name+"}", value);
			}
			
			RedirectResponse httpResponse = new RedirectResponse(null, r.domain, path);
			responseCb.sendRedirect(httpResponse);
		} else if(response instanceof RenderHtml) {
			//in the case where the POST route was found, the controller canNOT be returning RenderHtml and should follow PRG
			//If the POST route was not found, just render the notFound page that controller sends us
			if(!incomingRequestMeta.isNotFoundRoute() && HttpMethod.POST == r.method) {
				throw new IllegalReturnValueException("Controller method='"+method+"' MUST follow the PRG "
						+ "pattern(https://en.wikipedia.org/wiki/Post/Redirect/Get) so "
						+ "users don't have a poor experience using your website with the browser back button.  "
						+ "This means on a POST request, you cannot return RenderHtml object and must return Redirects");
			}
			RenderHtml renderHtml = (RenderHtml) response;
			
			RenderResponse resp = new RenderResponse(renderHtml.getView());
			responseCb.sendRenderHtml(resp);
		}
		return null;
	}
	
	private Map<String, String> formMap(Method method, List<String> pathParamNames, List<Object> args) {
		if(pathParamNames.size() != args.size())
			throw new IllegalReturnValueException("The Redirect object returned from method='"+method+"' has the wrong number of arguments. args.size="+args.size()+" should be size="+pathParamNames.size());

		Map<String, String> nameToValue = new HashMap<>();
		for(int i = 0; i < pathParamNames.size(); i++) {
			String key = pathParamNames.get(i);
			Object obj = args.get(i);
			if(obj != null) {
				//TODO: need reverse binding here!!!!
				//Anotherwords, apps register Converters String -> Object and Object to String and we should really be
				//using that instead of toString to convert which could be different
				nameToValue.put(key, obj.toString());
			}
		}
		return nameToValue;
	}

	private Object processException(ResponseStreamer responseCb, Throwable e) {
		if(e instanceof CompletionException) {
			//unwrap the exception to deliver the 'real' exception that occurred
			e = e.getCause();
		}
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

	public MatchResult fetchNotFoundRoute() {
		RouteInfo routerInfo = router.getRouterInfo();
		RouteMeta notfoundRoute = routerInfo.getPageNotfoundRoute();
		return new MatchResult(notfoundRoute);
	}
}
