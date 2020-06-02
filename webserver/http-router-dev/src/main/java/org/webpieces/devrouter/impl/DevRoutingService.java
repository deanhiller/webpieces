package org.webpieces.devrouter.impl;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.routes.WebAppMeta;
import org.webpieces.router.impl.AbstractRouterService;
import org.webpieces.router.impl.CookieTranslator;
import org.webpieces.router.impl.RouteLoader;
import org.webpieces.router.impl.WebInjector;
import org.webpieces.router.impl.params.ObjectTranslator;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;
import org.webpieces.router.impl.routers.ARouter;
import org.webpieces.util.cmdline2.Arguments;
import org.webpieces.util.file.VirtualFile;

import com.google.inject.Injector;
import com.webpieces.http2engine.api.StreamRef;
import com.webpieces.http2engine.api.StreamWriter;

public class DevRoutingService extends AbstractRouterService {

	private static final Logger log = LoggerFactory.getLogger(DevRoutingService.class);
	private static final Consumer<Injector> NO_OP = whatever -> {};
	
	private long lastFileTimestamp;
	private RouteLoader routeLoader;
	private DevClassForName classLoader;
	private WebAppMeta routerModule;
	private RouterConfig config;
	private ARouter router;
	private Arguments arguments;

	@Inject
	public DevRoutingService(
			RouteLoader routeConfig, 
			RouterConfig config, 
			ARouter router, 
			DevClassForName loader, 
			CookieTranslator cookieTranslator,
			ObjectTranslator objTranslator,
			WebInjector webInjector
	) {
		super(webInjector, routeConfig, cookieTranslator, objTranslator);
		this.routeLoader = routeConfig;
		this.config = config;
		this.router = router;
		this.classLoader = loader;
		this.lastFileTimestamp = config.getMetaFile().lastModified();
	}

	@Override
	public void configure(Arguments arguments) {
		this.arguments = arguments;
		routeLoader.configure(classLoader, arguments);
	}
	
	@Override
	public Injector start() {
		log.info("Starting DEVELOPMENT server with CompilingClassLoader and HotSwap");
		Injector inj = loadOrReload(injector -> runStartupHooks(injector));
		return inj;
	}

	@Override
	public RouterStreamRef incomingRequestImpl(RequestContext ctx, ProxyStreamHandle handler) {
		//In DevRouter, check if we need to reload the text file as it points to a new RouterModules.java implementation file
		boolean reloaded = reloadIfTextFileChanged();
		
		if(!reloaded)
			reloadIfClassFilesChanged();
		
		return router.invoke(ctx, handler);
	}

	/**
	 * Only used with DevRouterConfig which is not on classpath in prod mode
	 * 
	 * @return
	 */
	private boolean reloadIfTextFileChanged() {
		VirtualFile metaTextFile = config.getMetaFile();
		//if timestamp the same, no changes
		if(lastFileTimestamp == metaTextFile.lastModified())
			return false;

		log.info("text file changed so need to reload RouterModules.java implementation");

		routerModule = routeLoader.configure(classLoader, arguments);
		routeLoader.load(injector -> runStartupHooks(injector));
		lastFileTimestamp = metaTextFile.lastModified();
		return true;
	}

	private void reloadIfClassFilesChanged() {
		String routerModuleClassName = routerModule.getClass().getName();
		ClassLoader previousCl = routerModule.getClass().getClassLoader();
		
		Class<?> newClazz = classLoader.clazzForName(routerModuleClassName);
		ClassLoader newClassLoader = newClazz.getClassLoader();
		if(previousCl == newClassLoader)
			return;
		
		log.info("classloader change so we need to reload all router classes");
		loadOrReload(injector -> runStartupHooks(injector));
	}

	private Injector loadOrReload(Consumer<Injector> startupHook) {
		routerModule = routeLoader.configure(classLoader, arguments);
		return routeLoader.load(startupHook);
	}

}
