package org.webpieces.devrouter.impl;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.RouterService;
import org.webpieces.router.api.routes.WebAppMeta;
import org.webpieces.router.impl.AbstractRouterService;
import org.webpieces.router.impl.CookieTranslator;
import org.webpieces.router.impl.RouteLoader;
import org.webpieces.router.impl.loader.ControllerLoader;
import org.webpieces.router.impl.params.ObjectTranslator;
import org.webpieces.router.impl.routing.MasterRouter;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.google.inject.Injector;

public class DevRoutingService extends AbstractRouterService implements RouterService {

	private static final Logger log = LoggerFactory.getLogger(DevRoutingService.class);
	private static final Consumer<Injector> NO_OP = whatever -> {};
	
	private long lastFileTimestamp;
	private RouteLoader routeLoader;
	private DevClassForName classLoader;
	private WebAppMeta routerModule;
	private RouterConfig config;
	private MasterRouter router;

	@Inject
	public DevRoutingService(
			RouteLoader routeConfig, 
			RouterConfig config, 
			MasterRouter router, 
			DevClassForName loader, 
			CookieTranslator cookieTranslator,
			ObjectTranslator objTranslator
	) {
		super(routeConfig, cookieTranslator, objTranslator);
		this.routeLoader = routeConfig;
		this.config = config;
		this.router = router;
		this.classLoader = loader;
		this.lastFileTimestamp = config.getMetaFile().lastModified();
	}

	@Override
	public void start() {
		log.info("Starting DEVELOPMENT server with CompilingClassLoader and HotSwap");
		loadOrReload(injector -> runStartupHooks(injector)); 
		started = true;
	}

	@Override
	public void stop() {
		started = false;
	}
	
	@Override
	public CompletableFuture<Void> incomingRequestImpl(RequestContext ctx, ResponseStreamer responseCb) {
		//In DevRouter, check if we need to reload the text file as it points to a new RouterModules.java implementation file
		boolean reloaded = reloadIfTextFileChanged();
		
		if(!reloaded)
			reloadIfClassFilesChanged();
		
		return router.invoke(ctx, responseCb);
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

		routerModule = routeLoader.load(classLoader, NO_OP);
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
		loadOrReload(NO_OP);
	}

	private void loadOrReload(Consumer<Injector> startupHook) {
		routerModule = routeLoader.load(classLoader, startupHook);
	}

}
