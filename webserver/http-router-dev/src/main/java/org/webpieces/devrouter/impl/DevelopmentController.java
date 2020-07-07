package org.webpieces.devrouter.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.compiler.api.CompilationsException;
import org.webpieces.compiler.api.CompileError;
import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Render;
import org.webpieces.router.impl.RoutingHolder;
import org.webpieces.router.impl.routers.BRouter;
import org.webpieces.router.impl.routers.CRouter;

@Singleton
public class DevelopmentController {

	public static final String INTERNAL_ERROR_KEY = "webpiecesShowInternalErrorPage";
	public static final String NOT_FOUND_KEY = "webpiecesShowPage";


	public static final String ORIGINAL_REQUEST = "__originalRequest";
	public static final String EXCEPTION = "__exception";
	public static final String WEBPIECES_EXCCEPTION_KEY = "__webpiecesExceptionKey";
	
	private final RoutingHolder routingHolder;
	private ThrowableUtil throwableUtil = new ThrowableUtil();
	
	@Inject
	public DevelopmentController(RoutingHolder routingHolder) {
		super();
		this.routingHolder = routingHolder;
	}

	public Render internalError() {
		//Was for refresh page
//		if(Current.flash().containsKey(WEBPIECES_EXCCEPTION_KEY)) {
//			Current.flash().keep(true);
//			String url = Current.flash().get("url");
//			String compileErrorStr = Current.flash().get("compileErrors");
//			String newExcStr = Current.flash().get(WEBPIECES_EXCCEPTION_KEY);
//			return Actions.renderThis("url", url, "compileErrors", compileErrorStr, "exception", newExcStr);
//		}
		
		RouterRequest request = Current.request();
		String url = null;
		Object reloadError = request.requestState.get(DevRouteInvoker.ERROR_KEY);
		if(reloadError == null && request.method == HttpMethod.GET) {
			//we can only do this for GET right now.
			
			url = request.getSingleMultipart("url");
	
			if(url.contains("?")) {
				url += "&"+INTERNAL_ERROR_KEY+"=true";
			} else {
				url += "?"+INTERNAL_ERROR_KEY+"=true";
			}
		}
		
		Throwable exc = (Throwable) request.requestState.get(EXCEPTION);
		String exceptionStr = translate(exc);
		String newExcStr = exceptionStr.replace("\000", "<span style=\"color:red;\">").replace("\001", "</span>");
		String compileErrorStr = null;
		
		if(exc instanceof CompilationsException) {
			CompilationsException compileExc = (CompilationsException) exc;
			List<CompileError> compileErrors = compileExc.getCompileErrors();
			compileErrorStr = translate(compileErrors);
		}

		return Actions.renderThis("url", url, "compileErrors", compileErrorStr, "exception", newExcStr);
	}
	
	private String translate(List<CompileError> compileErrors) {
		String errors = "";
		for(CompileError error : compileErrors) {
			errors += "<br/>";
			errors += "File: "+error.getJavaFile().getCanonicalPath()+"\n";
			errors += "Class: "+error.getClassName()+"\n";
			errors += "<span style=\"color:red;\">Error: "+error.getProblem().getMessage()+"</span>\n\n";
			for(String line : error.getBadSourceLine()) {
				String newLine = line.replace("\000", "<span style=\"color:red;\">")
										.replace("\001", "</span>");
				
				errors += "    "+newLine+"</br>";
			}
		}
		
		return errors;
	}

	private String translate(Throwable exc) {
		return throwableUtil.translate(exc);
	}

	public Render notFound() {
		RouterRequest request = Current.request();
		String error = request.getSingleMultipart("webpiecesError");
		String url = request.getSingleMultipart("url");
		
		if(url.contains("?")) {
			url += "&"+NOT_FOUND_KEY+"=true";
		} else {
			url += "?"+NOT_FOUND_KEY+"=true";
		}

		Collection<CRouter> routers = new ArrayList<>();
		CRouter router;
		BRouter domainRouter = routingHolder.getDomainRouter();

		if(request.isBackendRequest) {
			router = domainRouter.getBackendRouter();
		} else {
			router = domainRouter.getLeftOverDomains();
			
			for(CRouter oneRouter : domainRouter.getDomainToRouter().values()) {
				routers.add(oneRouter);
			}
		}
		
		RouterRequest req = (RouterRequest) request.requestState.get(ORIGINAL_REQUEST);
		//This is a pain but dynamically build up the html
		String routeHtml = build(req.isHttps, req.method, req.relativePath, router);
		
		
		List<String> paths = new ArrayList<>();
		if(req.isHttps) {
			paths.add(req.method+" :https : "+req.relativePath);
		} else {
			paths.add(req.method+" :https : "+req.relativePath);
			paths.add(req.method+" :both  : "+req.relativePath);
		}
		
		return Actions.renderThis("domains", routers, "paths", paths, "routeHtml", routeHtml, "error", error, "url", url);
	}

	private String build(boolean isHttps, HttpMethod method, String path, CRouter mainRoutes) {
		return mainRoutes.buildHtml(isHttps, method, path, " ");
	}
	
	
}
