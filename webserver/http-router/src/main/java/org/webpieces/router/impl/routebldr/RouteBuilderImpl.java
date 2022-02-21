package org.webpieces.router.impl.routebldr;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.FilterPortType;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.router.impl.ResettingLogic;
import org.webpieces.router.impl.RouterFutureUtil;
import org.webpieces.router.impl.UrlPath;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.model.RouteBuilderLogic;
import org.webpieces.router.impl.model.RouterInfo;
import org.webpieces.router.impl.routers.AbstractRouter;
import org.webpieces.router.impl.routers.DScopedRouter;
import org.webpieces.router.impl.routers.EInternalErrorRouter;
import org.webpieces.router.impl.routers.ENotFoundRouter;
import org.webpieces.router.impl.routers.EScopedRouter;
import org.webpieces.router.impl.routers.FStaticRouter;
import org.webpieces.router.impl.routers.MatchInfo;
import org.webpieces.router.impl.services.SvcProxyFixedRoutes;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.util.file.VirtualFileFactory;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.FutureHelper;

public class RouteBuilderImpl extends ScopedRouteBuilderImpl implements RouteBuilder {

	private static final Logger log = LoggerFactory.getLogger(RouteBuilderImpl.class);

	private List<FilterInfo<?>> routeFilters = new ArrayList<>();
	private List<FilterInfo<?>> notFoundFilters = new ArrayList<>();
	private List<FilterInfo<?>> internalErrorFilters = new ArrayList<>();

	private final List<FStaticRouter> staticRouters = new ArrayList<>();

	private RouteInfo pageNotFoundInfo;
	private RouteInfo internalErrorInfo;

	private LoadedController notFoundControllerInst;
	private LoadedController internalErrorController;

	private FutureHelper futureUtil;
	private RouterFutureUtil routerFutures;
	
	public RouteBuilderImpl(String id, RouteBuilderLogic holder, ResettingLogic resettingLogic, FutureHelper futureUtil, RouterFutureUtil routerFutures) {
		super(new RouterInfo(id, ""), holder, resettingLogic, futureUtil, routerFutures);
		this.futureUtil = futureUtil;
		this.routerFutures = routerFutures;
	}

	@Override
	public <T> void addFilter(String path, Class<? extends RouteFilter<T>> filter, T initialConfig, FilterPortType type, int filterApplyLevel) {
		FilterInfo<T> info = new FilterInfo<>(path, filter, initialConfig, type, filterApplyLevel);
		routeFilters.add(info);
	}
	
	@Override
	public <T> void addPackageFilter(String regEx, Class<? extends RouteFilter<T>> filter, T initialConfig,
			FilterPortType type, int filterApplyLevel) {
		FilterInfo<T> info = new FilterInfo<>(regEx, true, filter, initialConfig, type, filterApplyLevel);
		routeFilters.add(info);		
	}
	
	@Override
	public <T> void addNotFoundFilter(Class<? extends RouteFilter<T>> filter, T initialConfig, FilterPortType type, int filterApplyLevel) {
		FilterInfo<T> info = new FilterInfo<>("", filter, initialConfig, type, filterApplyLevel);
		notFoundFilters.add(info);		
	}

	@Override
	public <T> void addInternalErrorFilter(Class<? extends RouteFilter<T>> filter, T initialConfig, FilterPortType type, int filterApplyLevel) {
		FilterInfo<T> info = new FilterInfo<>("", filter, initialConfig, type, filterApplyLevel);
		internalErrorFilters.add(info);		
	}

	@Override
	public void setPageNotFoundRoute(String controllerMethod) {
		if(pageNotFoundInfo != null)
			throw new IllegalStateException("Page Not found for domain="+routerInfo.getRouterId()+" was already set.  cannot set again.  previous="+pageNotFoundInfo);
		RouteInfo route = new RouteInfo(CurrentRoutes.get(), controllerMethod);
		log.info("scope:'"+routerInfo+"' adding PAGE_NOT_FOUND route method="+route.getControllerMethodString());
		
		//MUST DO loadController HERE so stack trace has customer's line in it so he knows EXACTLY what 
		//he did wrong when reading the exception!!
		this.notFoundControllerInst = holder.getFinder().loadNotFoundController(resettingLogic.getInjector(), route);
		this.pageNotFoundInfo = route;
	}

	@Override
	public void setInternalErrorRoute(String controllerMethod) {
		if(internalErrorInfo != null)
			throw new IllegalStateException("Internal Error Route for domain="+routerInfo.getRouterId()+" was already set.  cannot set again");
		RouteInfo route = new RouteInfo(CurrentRoutes.get(), controllerMethod);
		log.info("scope:'"+routerInfo+"' adding INTERNAL_SVR_ERROR route method="+route.getControllerMethodString());
		
		//MUST DO loadController HERE so stack trace has customer's line in it so he knows EXACTLY what 
		//he did wrong when reading the exception!!
		this.internalErrorController = holder.getFinder().loadErrorController(resettingLogic.getInjector(), route);
		this.internalErrorInfo = route;
	}

	@Override
	public void addStaticDir(Port port, String urlPath, String fileSystemPath, boolean isOnClassPath) {
		//urlPath must start with / and not end with / but we must allow just '/' which technicall
		//ends and begins with /
		if(!urlPath.endsWith("/") && !"/".equals(urlPath))
			throw new IllegalArgumentException("Static directory so urlPath must end with a /");
		addStaticRoute(port, urlPath, fileSystemPath, isOnClassPath);
	}

	@Override
	public void addStaticFile(Port port, String urlPath, String fileSystemPath, boolean isOnClassPath) {
		//urlPath must start with / and not end with / but we must allow just '/' which technicall
		//ends and begins with /
		if(urlPath.endsWith("/") && !"/".equals(urlPath))
			throw new IllegalArgumentException("Static file so urlPath must NOT end with a /");
		addStaticRoute(port, urlPath, fileSystemPath, isOnClassPath);
	}
	
	private void addStaticRoute(Port port, String urlPath, String fileSystemPath, boolean isOnClassPath) {
		if(!urlPath.startsWith("/"))
			throw new IllegalArgumentException("static resource url paths must start with / path="+urlPath);
		
		if(isOnClassPath)
			addStaticClasspathFile(port, urlPath, fileSystemPath);
		else
			addStaticLocalFile(port, urlPath, fileSystemPath);
	}
	
	private void addStaticClasspathFile(Port port, String urlSubPath, String fileSystemPath) {
		if(!fileSystemPath.startsWith("/"))
			throw new IllegalArgumentException("Classpath resources must start with a / and be absolute on the classpath");
		
		boolean isDirectory = fileSystemPath.endsWith("/");
		VirtualFile file = new VirtualFileClasspath(fileSystemPath, getClass(), isDirectory);
		
		UrlPath p = new UrlPath(routerInfo, urlSubPath);

		//we can only verify files, not directories :(
		if(!isDirectory && !file.exists())
			throw new IllegalArgumentException("Static File="+file.getCanonicalPath()+" does not exist. fileSysPath="+file+" abs="+file.getAbsolutePath());

		createStaticRouter(p, port, HttpMethod.GET, holder.getUrlEncoding(), file, true);
	}
	private void addStaticLocalFile(Port port, String path, String fileSystemPath) {
		if(fileSystemPath.startsWith("/"))
			throw new IllegalArgumentException("Absolute file system path is not supported as it is not portable across OS when done wrong.  Override the modules working directory instead");
		
		File workingDir = holder.getConfig().getWorkingDirectory();
		VirtualFile file = VirtualFileFactory.newFile(workingDir, fileSystemPath);
		UrlPath p = new UrlPath(routerInfo, path);
		
		if(!file.exists())
			throw new IllegalArgumentException("Static File="+file.getCanonicalPath()+" does not exist. fileSysPath="+file+" abs="+file.getAbsolutePath());

		createStaticRouter(p, port, HttpMethod.GET, holder.getUrlEncoding(), file, false);
	}
	
	private void createStaticRouter(UrlPath urlPath, Port exposedPort, HttpMethod httpMethod, Charset urlEncoding, VirtualFile file, boolean isOnClassPath) {

		String urlSubPath = urlPath.getSubPath();
		List<String> pathParamNames = new ArrayList<>();
		Pattern patternToMatch;
		boolean isFile;
		if(file.isDirectory()) {
			//if file on file system is directory, then we cannot be adding a static looking url!!!
			if(!isDirectory(urlSubPath))
				throw new IllegalArgumentException("Static directory so urlPath must end with a / to map to directory="+file);

			patternToMatch = Pattern.compile("^"+urlSubPath+"(?<resource>.*)$");
			pathParamNames.add("resource");
			isFile = false;
		} else if(!file.isFile()) {
			//it should be a file if not a directory but let's make sure
			throw new IllegalArgumentException("file="+file.getCanonicalPath()+" is not a file and must be for static file route");
		} else {
			//it is a file
			patternToMatch = Pattern.compile("^"+urlSubPath+"$");
			isFile = true;
		}
		
		MatchInfo matchInfo = new MatchInfo(urlPath, exposedPort, httpMethod, urlEncoding, patternToMatch, pathParamNames);
		String relativePath = urlSubPath.substring(1);
		File targetCacheLocation = FileFactory.newFile(holder.getCachedCompressedDirectory(), relativePath);
		FStaticRouter router = new FStaticRouter(holder.getRouteInvoker2(), matchInfo, file, isOnClassPath, targetCacheLocation, isFile);
		staticRouters.add(router);
		log.info("scope:'"+routerInfo+"' added route="+matchInfo+" fileSystemPath="+file);
	}
	
	private boolean isDirectory(String urlSubPath) {
		return urlSubPath.endsWith("/");
	}
	
	public Collection<? extends FStaticRouter> getStaticRoutes() {
		return staticRouters;
	}
	
	public DScopedRouter buildRouter() {
		if(pageNotFoundInfo == null)
			throw new IllegalStateException("Client did not call setPageNotFoundRoute for router="+routerInfo+" and that's required to catch stray not founds");
		else if(internalErrorInfo == null)
			throw new IllegalStateException("Client did not call setInternalErrorRoute for router="+routerInfo+" and that's required to catch stray bugs in your application");
		
		List<AbstractRouter> routers = super.buildRoutes(routeFilters);
		
		//static routes get cached in browser typically so add them last so dynamic routes which are not cached are 
		//pattern matched first and we don't waste loop matching static routes
		routers.addAll(staticRouters);

		Map<String, EScopedRouter> pathToRouter = buildScopedRouters(routeFilters);

		SvcProxyFixedRoutes svcProxy = new SvcProxyFixedRoutes(holder.getSvcProxyLogic().getServiceInvoker(), futureUtil);

		FilterCreationMeta notFoundChain = new FilterCreationMeta(resettingLogic.getInjector(), notFoundFilters, svcProxy);
		FilterCreationMeta internalErrorChain = new FilterCreationMeta(resettingLogic.getInjector(), internalErrorFilters, svcProxy);

		Service<MethodMeta, Action> svc = holder.getFinder().loadFilters(internalErrorChain);
		String i18nBundleName = internalErrorInfo.getRouteModuleInfo().getI18nBundleName();
		EInternalErrorRouter internalErrorRouter = new EInternalErrorRouter(holder.getRouteInvoker2(), i18nBundleName, internalErrorController, svc);

		Service<MethodMeta, Action> notFoundSvc = holder.getFinder().loadFilters(notFoundChain);
		String notFoundBundleName = pageNotFoundInfo.getRouteModuleInfo().getI18nBundleName();
		ENotFoundRouter notFoundRouter = new ENotFoundRouter(holder.getRouteInvoker2(), notFoundBundleName, notFoundControllerInst, notFoundSvc);
	
		return new DScopedRouter(routerInfo, pathToRouter, routers, notFoundRouter, internalErrorRouter, routerFutures, futureUtil);
	}

}
