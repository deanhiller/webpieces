package org.webpieces.router.impl.routebldr;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routebldr.ScopedRouteBuilder;
import org.webpieces.router.api.routes.CrudRouteIds;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.RouteId;
import org.webpieces.router.impl.ResettingLogic;
import org.webpieces.router.impl.UrlPath;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.loader.BinderAndLoader;
import org.webpieces.router.impl.loader.MethodMetaAndController;
import org.webpieces.router.impl.model.RouteBuilderLogic;
import org.webpieces.router.impl.model.RouterInfo;
import org.webpieces.router.impl.routers.AbstractRouter;
import org.webpieces.router.impl.routers.EScopedRouter;
import org.webpieces.router.impl.routers.FContentRouter;
import org.webpieces.router.impl.routers.FHtmlRouter;
import org.webpieces.router.impl.routers.FStaticRouter;
import org.webpieces.router.impl.routers.MatchInfo;
import org.webpieces.router.impl.services.SvcProxyForContent;
import org.webpieces.router.impl.services.SvcProxyForHtml;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileClasspath;
import org.webpieces.util.file.VirtualFileFactory;

public class ScopedRouteBuilderImpl extends SharedMatchUtil implements ScopedRouteBuilder {

	private static final Logger log = LoggerFactory.getLogger(ScopedRouteBuilderImpl.class);

	protected final ResettingLogic resettingLogic;
	protected final RouteBuilderLogic holder;
	protected final RouterInfo routerInfo;
	private final Map<String, ScopedRouteBuilderImpl> pathToBuilder = new HashMap<>();

	private final List<FStaticRouter> staticRouters = new ArrayList<>();

	//private final List<StaticRouteMeta> staticRoutes = new ArrayList<>();
	//private List<StaticRoute> allStaticRoutes;
	
	public ScopedRouteBuilderImpl(RouterInfo routerInfo, RouteBuilderLogic holder, ResettingLogic resettingLogic) {
		super(holder, resettingLogic);
		this.routerInfo = routerInfo;
		this.holder = holder;
		this.resettingLogic = resettingLogic;
	}

	@Override
	public void addContentRoute(Port port, HttpMethod method, String path, String controllerMethod) {
		addContentRoute(port, method, path, controllerMethod, null);
	}
	
	@Override
	public void addContentRoute(Port port, HttpMethod method, String path, String controllerMethod, RouteId routeId) {
		UrlPath p = new UrlPath(routerInfo, path);
		RouteInfo routeInfo = new RouteInfo(CurrentPackage.get(), controllerMethod);
		//MUST DO loadControllerIntoMeta HERE so stack trace has customer's line in it so he knows EXACTLY what 
		//he did wrong when reading the exception!!
		BinderAndLoader container = holder.getFinder().loadContentController(resettingLogic.getInjector(), routeInfo, true);
		
		MatchInfo matchInfo = createMatchInfo(p, port, method, holder.getUrlEncoding());
		FContentRouter router = new FContentRouter(holder.getRouteInvoker2(), matchInfo, container.getBinder());
		SvcProxyForContent svc = new SvcProxyForContent(holder.getSvcProxyLogic());
		RouterAndInfo routerAndInfo = new RouterAndInfo(router, routeInfo, RouteType.HTML, container.getMetaAndController(), svc);
		
		newDynamicRoutes.add(routerAndInfo);
		if(routeId != null) //if there is a routeId, then add the reverse mapping
			resettingLogic.getReverseRoutes().addRoute(routeId, router);

		log.info("scope:'"+routerInfo+"' added content route="+matchInfo+" method="+routeInfo.getControllerMethodString());
	}
	
	private MatchInfo createMatchInfo(UrlPath urlPath, Port exposedPort, HttpMethod httpMethod, Charset urlEncoding) {
		RegExResult result = RegExUtil.parsePath(urlPath.getSubPath());
		Pattern patternToMatch = Pattern.compile(result.regExToMatch);
		List<String> pathParamNames = result.argNames;
		return new MatchInfo(urlPath, exposedPort, httpMethod, urlEncoding, patternToMatch, pathParamNames);
	}
	
	@Override
	public void addRoute(Port port, HttpMethod method, String path, String controllerMethod, RouteId routeId) {
		boolean checkSecureToken = false;
		if(method == HttpMethod.POST)
			checkSecureToken = true;
		addRoute(port, method, path, controllerMethod, routeId, checkSecureToken);
	}

	@Override
	public void addRoute(Port port, HttpMethod method, String path, String controllerMethod, RouteId routeId,
			boolean checkToken) {
		if(!controllerMethod.contains("."))
			throw new IllegalArgumentException("controllerMethod='"+controllerMethod+" does not contain a '.' which is required separating class name and method");
		UrlPath p = new UrlPath(routerInfo, path);

		boolean isPostOnly = method == HttpMethod.POST;
		RouteInfo routeInfo = new RouteInfo(CurrentPackage.get(), controllerMethod);

		//MUST DO loadControllerIntoMetat HERE so stack trace has customer's line in it so he knows EXACTLY what 
		//he did wrong when reading the exception!!
		MethodMetaAndController metaAndController = holder.getFinder().loadHtmlController(resettingLogic.getInjector(), routeInfo, true, isPostOnly);
		
		MatchInfo matchInfo = createMatchInfo(p, port, method, holder.getUrlEncoding());
		FHtmlRouter router = new FHtmlRouter(holder.getRouteInvoker2(), matchInfo, checkToken);	
		SvcProxyForHtml svc = new SvcProxyForHtml(holder.getSvcProxyLogic());
		RouterAndInfo routerAndInfo = new RouterAndInfo(router, routeInfo, RouteType.HTML, metaAndController, svc);
		
		newDynamicRoutes.add(routerAndInfo);
		resettingLogic.getReverseRoutes().addRoute(routeId, router);
		
		log.info("scope:'"+routerInfo+"' added route="+matchInfo+" method="+routeInfo.getControllerMethodString());
	}

	/*
	 * Adds routes like the following all in one method
	 * 
	 * 	//addRoute(GET ,   "/user/list",        "crud/CrudUserController.userList", listRoute);
	 *	//addRoute(GET ,   "/user/new",         "crud/CrudUserController.userAddEdit", addRoute);
	 *	//addRoute(GET ,   "/user/edit/{id}",   "crud/CrudUserController.userAddEdit", editRoute);
	 *	//addRoute(POST,   "/user/post",        "crud/CrudUserController.postSaveUser", saveRoute);
	 *	//addRoute(GET,    "/user/delete/{id}", "crud/CrudUserController.postDeleteUser", deleteRoute);
	 */
	@Override
	public void addCrud(Port port, String entity, String controller, CrudRouteIds routeIds) {
		RouteId listRoute = routeIds.getListRoute();
		RouteId addRoute = routeIds.getAddRoute();
		RouteId editRoute = routeIds.getEditRoute(); 
		RouteId postSaveRoute = routeIds.getPostSaveRoute();
		RouteId confirmDelete = routeIds.getConfirmDelete();
		RouteId postDeleteRoute = routeIds.getPostDeleteRoute();
		
		String entityWithCapital = entity.substring(0, 1).toUpperCase() + entity.substring(1);
		addRoute(port, GET , "/"+entity+"/list",        controller+"."+entity+"List", listRoute);
		addRoute(port, GET , "/"+entity+"/new",         controller+"."+entity+"AddEdit", addRoute);
		addRoute(port, GET , "/"+entity+"/edit/{id}",   controller+"."+entity+"AddEdit", editRoute);
		addRoute(port, POST, "/"+entity+"/post",        controller+".postSave"+entityWithCapital, postSaveRoute);

		//get the confirm delete page
		addRoute(port, GET,  "/"+entity+"/confirmdelete/{id}", controller+".confirmDelete"+entityWithCapital, confirmDelete);
		//NOTE: Browsers don't support DELETE.  POST might make more sense here for delete but GET is way way less html
		//code(ok, 1 line instead of 3).  There are hacks with javascript to support DELETE but seriously, we should just
		//KISS and YAGNI (google that if you don't know).  
		//HOWEVER, If you don't like this, copy and paste this method and modify to be a POST OR DELETE and add the 
		//javascript for next time
		addRoute(port, POST, "/"+entity+"/delete/{id}", controller+".postDelete"+entityWithCapital, postDeleteRoute);
	}
	
	@Override
	public void addStaticDir(Port port, String urlPath, String fileSystemPath, boolean isOnClassPath) {
		if(!urlPath.endsWith("/"))
			throw new IllegalArgumentException("Static directory so urlPath must end with a /");
		addStaticRoute(port, urlPath, fileSystemPath, isOnClassPath);
	}

	@Override
	public void addStaticFile(Port port, String urlPath, String fileSystemPath, boolean isOnClassPath) {
		if(urlPath.endsWith("/"))
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
		if(isDirectory(urlSubPath)) {
			if(!file.isDirectory())
				throw new IllegalArgumentException("Static directory so fileSystemPath must end with a /");
			else if(!file.isDirectory())
				throw new IllegalArgumentException("file="+file.getCanonicalPath()+" is not a directory and must be for static directories");
			patternToMatch = Pattern.compile("^"+urlSubPath+"(?<resource>.*)$");
			pathParamNames.add("resource");
			isFile = false;
		} else {
			if(file.isDirectory())
				throw new IllegalArgumentException("Static file so fileSystemPath must NOT end with a /");
			else if(!file.isFile())
				throw new IllegalArgumentException("file="+file.getCanonicalPath()+" is not a file and must be for static file route");
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
	
	/**
	 * Note, if someone passes in /car/civic, we have to create a ScopedRouteBuilder and
	 * then call getScopedRouteBuilder("/civic") on that so that lookups work when they
	 * do getScopedRouteBuilder("/car") in another module.
	 */
	@Override
	public ScopedRouteBuilder getScopedRouteBuilder(String fullPath) {
		if(fullPath == null)
			throw new IllegalArgumentException("path cannot be null");
		else if(!fullPath.startsWith("/"))
			throw new IllegalArgumentException("path must start with /");
		else if(fullPath.endsWith("/"))
			throw new IllegalArgumentException("path must not end with /");
		else if(fullPath.length() <= 1)
			throw new IllegalArgumentException("path size must be greater than 1");
		
		String[] split = splitInTwo(fullPath);
		String path = fullPath;
		if(split != null)
			path = split[0];
		
		ScopedRouteBuilderImpl r = pathToBuilder.get(path);
		if(r == null) {
			r = new ScopedRouteBuilderImpl(new RouterInfo(routerInfo.getRouterId(), routerInfo.getPath()+path), holder, resettingLogic);
			pathToBuilder.put(path, r);
		}
		
		if(split == null)
			return r;
		else
			return r.getScopedRouteBuilder(split[1]);
	}
	
	private String[] splitInTwo(String fullPath) {
		if(!fullPath.startsWith("/"))
			throw new IllegalArgumentException("fullPath should start with a / but did not");
		
		int indexOf = fullPath.indexOf("/", 1);
		if(indexOf < 0)
			return null;
		
		String path = fullPath.substring(0, indexOf);
		String leftover = fullPath.substring(indexOf);
		return new String[] {path, leftover};
	}
	
	public Collection<? extends FStaticRouter> getStaticRoutes() {
		List<FStaticRouter> allStaticRouters = new ArrayList<>();
		for(Entry<String, ScopedRouteBuilderImpl> entry : pathToBuilder.entrySet()) {
			allStaticRouters.addAll(entry.getValue().getStaticRoutes());
		}
		
		allStaticRouters.addAll(staticRouters);
		return allStaticRouters;
	}
	
	public EScopedRouter build(List<FilterInfo<?>> routeFilters) {
		List<AbstractRouter> routers = buildRoutes(routeFilters);
		
		Map<String, EScopedRouter> pathToRouter = buildScopedRouters(routeFilters);
		
		return new EScopedRouter(routerInfo, pathToRouter, routers);
	}

	protected Map<String, EScopedRouter> buildScopedRouters(List<FilterInfo<?>> routeFilters) {
		Map<String, EScopedRouter> pathToRouter = new HashMap<>();
		for(Entry<String, ScopedRouteBuilderImpl> entry : pathToBuilder.entrySet()) {
			EScopedRouter router2 = entry.getValue().build(routeFilters);
			pathToRouter.put(entry.getKey(), router2);
		}
		return pathToRouter;
	}

	protected List<AbstractRouter> buildRoutes(List<FilterInfo<?>> routeFilters) {
		List<AbstractRouter> routers = super.buildRoutes(routeFilters);
		
		//static routes get cached in browser typically so add them last so dynamic routes which are not cached are 
		//pattern matched first
		routers.addAll(staticRouters);
		return routers;
	}

}
