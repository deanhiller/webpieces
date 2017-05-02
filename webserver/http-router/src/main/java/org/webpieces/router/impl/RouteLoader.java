package org.webpieces.router.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.router.api.routing.Plugin;
import org.webpieces.router.api.routing.Routes;
import org.webpieces.router.api.routing.WebAppMeta;
import org.webpieces.router.impl.compression.CompressionCacheSetup;
import org.webpieces.router.impl.compression.FileMeta;
import org.webpieces.router.impl.hooks.ClassForName;
import org.webpieces.router.impl.loader.ControllerLoader;
import org.webpieces.router.impl.model.AbstractRouteBuilder;
import org.webpieces.router.impl.model.L1AllRouting;
import org.webpieces.router.impl.model.LogicHolder;
import org.webpieces.router.impl.model.MatchResult;
import org.webpieces.router.impl.model.R1RouterBuilder;
import org.webpieces.router.impl.model.RouteModuleInfo;
import org.webpieces.router.impl.model.RouterInfo;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.filters.Service;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class RouteLoader {
	private static final Logger log = LoggerFactory.getLogger(RouteLoader.class);
	
	private final RouterConfig config;
	private final RouteInvoker invoker;
	private final ControllerLoader controllerFinder;
	private CompressionCacheSetup compressionCacheSetup;
	
	protected R1RouterBuilder routerBuilder;

	private PluginSetup pluginSetup;
	
	@Inject
	public RouteLoader(
		RouterConfig config, 
		RouteInvoker invoker,
		ControllerLoader controllerFinder,
		CompressionCacheSetup compressionCacheSetup,
		PluginSetup pluginSetup
	) {
		this.config = config;
		this.invoker = invoker;
		this.controllerFinder = controllerFinder;
		this.compressionCacheSetup = compressionCacheSetup;
		this.pluginSetup = pluginSetup;
	}
	
	public WebAppMeta load(ClassForName loader, Consumer<Injector> startupFunction) {
		try {
			return loadImpl(loader, startupFunction);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private WebAppMeta loadImpl(ClassForName loader, Consumer<Injector> startupFunction) throws IOException {
		log.info("loading the master "+WebAppMeta.class.getSimpleName()+" class file");

		VirtualFile fileWithMetaClassName = config.getMetaFile();
		String moduleName;
		
		Charset fileEncoding = config.getFileEncoding();
		String contents = fileWithMetaClassName.contentAsString(fileEncoding);
		moduleName = contents.trim();
		
		log.info(WebAppMeta.class.getSimpleName()+" class to load="+moduleName);
		Class<?> clazz = loader.clazzForName(moduleName);
		
		//In development mode, the ClassLoader here will be the CompilingClassLoader so stuff it into the thread context
		//just in case plugins will need it(most won't, hibernate will).  In production, this is really a no-op and does
		//nothing.  I don't like dev code existing in production :( so perhaps we should abstract this out at some 
		//point perhaps with a lambda of a lambda function
		ClassLoader original = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(clazz.getClassLoader());
		
			Object obj = newInstance(clazz);
			if(!(obj instanceof WebAppMeta))
				throw new IllegalArgumentException("name="+moduleName+" does not implement "+WebAppMeta.class.getSimpleName());
	
			log.info(WebAppMeta.class.getSimpleName()+" loaded.  initializing next");
	
			WebAppMeta routerModule = (WebAppMeta) obj;
			routerModule.initialize(config.getWebAppMetaProperties());
			log.info(WebAppMeta.class.getSimpleName()+" initialized.");
			
			Injector injector = createInjector(routerModule);
				
			pluginSetup.wireInPluginPoints(injector, startupFunction);
			
			loadAllRoutes(routerModule, injector);
			
			return routerModule;
		} finally {
			Thread.currentThread().setContextClassLoader(original);
		}
	}

	public Injector createInjector(WebAppMeta routerModule) {
		List<Module> guiceModules = routerModule.getGuiceModules();
		if(guiceModules == null)
			guiceModules = new ArrayList<>();
		
		guiceModules.add(new EmptyPluginModule());
		
		Module module = Modules.combine(guiceModules);
		
		List<Plugin> plugins = routerModule.getPlugins();
		if(plugins != null) {
			for(Plugin plugin : plugins) {
				List<Module> modules = new ArrayList<>();
				modules.addAll(plugin.getGuiceModules());
				modules.add(module);
				module = Modules.combine(modules);
			}
		}

		if(config.getWebappOverrides() != null)
			module = Modules.override(module).with(config.getWebappOverrides());
		
		Injector injector = Guice.createInjector(module);
		return injector;
	}
	
	//protected abstract void verifyRoutes(Collection<Route> allRoutes);

	public void loadAllRoutes(WebAppMeta rm, Injector injector) {
		log.info("adding routes");
		
		ReverseRoutes reverseRoutes = new ReverseRoutes(config);
		//routerBuilder = new RouterBuilder("", new AllRoutingInfo(), reverseRoutes, controllerFinder, config.getUrlEncoding());
		LogicHolder holder = new LogicHolder(reverseRoutes, controllerFinder, injector, config);
		routerBuilder = new R1RouterBuilder(new RouterInfo(null, ""), new L1AllRouting(), holder, false);
		invoker.init(reverseRoutes);
		
		List<Routes> all = new ArrayList<>();
		all.addAll(rm.getRouteModules()); //the core application routes
		
		List<Plugin> plugins = rm.getPlugins();
		if(plugins != null) {
			for(Plugin plugin : plugins) {
				all.addAll(plugin.getRouteModules());
			}
		}
		
		for(Routes module : all) {
			AbstractRouteBuilder.currentPackage.set(new RouteModuleInfo(module));
			module.configure(routerBuilder);
			AbstractRouteBuilder.currentPackage.set(null);
		}
		
		log.info("added all routes to router.  Applying Filters");

		reverseRoutes.finalSetup();
		
		routerBuilder.applyFilters(rm);
		
		Collection<RouteMeta> metas = reverseRoutes.getAllRouteMetas();
		for(RouteMeta m : metas) {
			controllerFinder.loadFiltersIntoMeta(m, m.getFilters(), true);
		}
		
		routerBuilder.loadNotFoundAndErrorFilters();
	
		log.info("all filters applied");
		
		compressionCacheSetup.setupCache(routerBuilder.getStaticRoutes());
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

	public MatchResult fetchRoute(RouterRequest req) {
		L1AllRouting allRoutingInfo = routerBuilder.getRouterInfo();
		MatchResult meta = allRoutingInfo.fetchRoute(req, req.relativePath);
		if(meta == null)
			throw new IllegalStateException("missing exception on creation if we go this far");

		return meta;
	}
	
	public CompletableFuture<Void> invokeRoute(MatchResult result, RequestContext routerRequest, ResponseStreamer responseCb, ErrorRoutes errorRoutes) {
		//This class is purely the RouteLoader so delegate and encapsulate the invocation in RouteInvoker....
		return invoker.invoke(result, routerRequest, responseCb, errorRoutes);
	}

	public Void processException(ResponseStreamer responseCb, RequestContext requestCtx, Throwable e, ErrorRoutes errorRoutes, Object meta) {
		return invoker.processException(responseCb, requestCtx, e, errorRoutes, meta);
	}
	
	public RouteMeta fetchNotFoundRoute(String domain) {
		L1AllRouting routerInfo = routerBuilder.getRouterInfo();
		RouteMeta notfoundRoute = routerInfo.getPageNotfoundRoute(domain);
		return notfoundRoute;
	}

	public RouteMeta fetchInternalErrorRoute(String domain) {
		L1AllRouting routerInfo = routerBuilder.getRouterInfo();
		RouteMeta internalErrorRoute = routerInfo.getInternalErrorRoute(domain);
		return internalErrorRoute;
	}

	public  Service<MethodMeta, Action> createNotFoundService(RouteMeta m, RouterRequest req) {
		List<FilterInfo<?>> filterInfos = routerBuilder.findNotFoundFilters(req.relativePath, req.isHttps);
		return controllerFinder.createNotFoundService(m, filterInfos);
	}

	public String convertToUrl(String routeId, Map<String, String> args, boolean isValidating) {
		return invoker.convertToUrl(routeId, args, isValidating);
	}
	
	public FileMeta relativeUrlToHash(String urlPath) {
		return compressionCacheSetup.relativeUrlToHash(urlPath);
	}
	
}
