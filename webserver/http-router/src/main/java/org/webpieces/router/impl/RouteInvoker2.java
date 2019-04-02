package org.webpieces.router.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.Messages;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.PortConfig;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.RenderContent;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.impl.actions.AjaxRedirectImpl;
import org.webpieces.router.impl.actions.RawRedirect;
import org.webpieces.router.impl.actions.RedirectImpl;
import org.webpieces.router.impl.actions.RenderImpl;
import org.webpieces.router.impl.ctx.RequestLocalCtx;
import org.webpieces.router.impl.ctx.ResponseProcessor;
import org.webpieces.router.impl.ctx.ResponseStaticProcessor;
import org.webpieces.router.impl.dto.RenderStaticResponse;
import org.webpieces.router.impl.loader.ControllerLoader;
import org.webpieces.router.impl.loader.svc.RouteInfoGeneric;
import org.webpieces.router.impl.loader.svc.RouteInfoForInternalError;
import org.webpieces.router.impl.loader.svc.RouteInfoForNotFound;
import org.webpieces.router.impl.loader.svc.LoadedController2;
import org.webpieces.router.impl.loader.svc.MethodMeta;
import org.webpieces.router.impl.loader.svc.RouteInfoForContent;
import org.webpieces.router.impl.model.MatchResult;
import org.webpieces.router.impl.params.ObjectToParamTranslator;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.filters.Service;

public class RouteInvoker2 {

	private ReverseRoutes reverseRoutes;

	private final ObjectToParamTranslator reverseTranslator;
	protected final RouterConfig config;
	protected final ControllerLoader controllerFinder;

	
	private volatile PortConfig portConfig;

	@Inject
	public RouteInvoker2(
		ObjectToParamTranslator reverseTranslator,
		RouterConfig config,
		ControllerLoader controllerFinder
	) {
		this.reverseTranslator = reverseTranslator;
		this.config = config;
		this.controllerFinder = controllerFinder;
	}
	
	public CompletableFuture<Void> invokeStatic(StaticRoute route, Map<String, String> pathParams, RequestContext ctx, ResponseStreamer responseCb) {

		boolean isOnClassPath = route.getIsOnClassPath();

		RenderStaticResponse resp = new RenderStaticResponse(route.getTargetCacheLocation(), isOnClassPath);
		if(route.isFile()) {
			resp.setFilePath(route.getFileSystemPath());
		} else {
			String relativeUrl = pathParams.get("resource");
			VirtualFile fullPath = route.getFileSystemPath().child(relativeUrl);
			resp.setFileAndRelativePath(fullPath, relativeUrl);
		}

		ResponseStaticProcessor processor = new ResponseStaticProcessor(ctx, responseCb);

		return processor.renderStaticResponse(resp);
	}

	//TODO: Collapse ALL these methods back down
	public CompletableFuture<Void> invokeErrorRoute(MatchResult result, RequestContext requestCtx, ResponseStreamer responseCb) {
		RouteMeta meta = result.getMeta();
		Service<MethodMeta, Action> service = meta.getService222();
		
		if(portConfig == null)
			portConfig = config.getPortConfigCallback().fetchPortConfig();
		ResponseProcessor processor = new ResponseProcessor(requestCtx, reverseRoutes, reverseTranslator, meta, responseCb, portConfig);
		
		Object obj = meta.getControllerInstance();
		if(obj == null)
			throw new IllegalStateException("Someone screwed up, as controllerInstance should not be null at this point, bug");
		Method method = meta.getMethod();

		if(service == null)
			throw new IllegalStateException("Bug, service should never be null at this point");
		
		Messages messages = new Messages(meta.getI18nBundleName(), "webpieces");
		requestCtx.setMessages(messages);

		RequestLocalCtx.set(processor);
		Current.setContext(requestCtx);
		
		MethodMeta methodMeta = new MethodMeta(new LoadedController2(obj, method), Current.getContext(), new RouteInfoForInternalError());
		CompletableFuture<Action> response;
		try {
			response = service.invoke(methodMeta);
		} finally {
			RequestLocalCtx.set(null);
			Current.setContext(null);
		}
		
		CompletableFuture<Void> future = response.thenCompose(resp -> continueProcessing(processor, resp, responseCb));
		return future;
	}
	
	public CompletableFuture<Void> invokeNotFound(MatchResult result, RequestContext requestCtx, ResponseStreamer responseCb, NotFoundException exc) {
		RouteMeta meta = result.getMeta();
		Service<MethodMeta, Action> service = createNotFoundService(meta, requestCtx.getRequest());
		
		if(portConfig == null)
			portConfig = config.getPortConfigCallback().fetchPortConfig();
		ResponseProcessor processor = new ResponseProcessor(requestCtx, reverseRoutes, reverseTranslator, meta, responseCb, portConfig);
		
		Object obj = meta.getControllerInstance();
		if(obj == null)
			throw new IllegalStateException("Someone screwed up, as controllerInstance should not be null at this point, bug");
		Method method = meta.getMethod();

		if(service == null)
			throw new IllegalStateException("Bug, service should never be null at this point");
		
		Messages messages = new Messages(meta.getI18nBundleName(), "webpieces");
		requestCtx.setMessages(messages);

		RequestLocalCtx.set(processor);
		Current.setContext(requestCtx);
		CompletableFuture<Action> response;
		try {
			response = invokeNotFoundMethod(service, obj, method, meta);
		} finally {
			RequestLocalCtx.set(null);
			Current.setContext(null);
		}
		
		CompletableFuture<Void> future = response.thenCompose(resp -> continueProcessing(processor, resp, responseCb));
		return future;
	}

	public CompletableFuture<Void> invokeContentController(MatchResult result, RequestContext requestCtx, ResponseStreamer responseCb) {
		RouteMeta meta = result.getMeta();
		Service<MethodMeta, Action> service = meta.getService222();
		
		if(portConfig == null)
			portConfig = config.getPortConfigCallback().fetchPortConfig();
		ResponseProcessor processor = new ResponseProcessor(requestCtx, reverseRoutes, reverseTranslator, meta, responseCb, portConfig);
		
		Object obj = meta.getControllerInstance();
		if(obj == null)
			throw new IllegalStateException("Someone screwed up, as controllerInstance should not be null at this point, bug");
		Method method = meta.getMethod();

		if(service == null)
			throw new IllegalStateException("Bug, service should never be null at this point");
		
		Messages messages = new Messages(meta.getI18nBundleName(), "webpieces");
		requestCtx.setMessages(messages);

		RequestLocalCtx.set(processor);
		Current.setContext(requestCtx);
		CompletableFuture<Action> response;
		try {
			response = invokeContentMethod(service, obj, method, meta);
		} finally {
			RequestLocalCtx.set(null);
			Current.setContext(null);
		}
		
		CompletableFuture<Void> future = response.thenCompose(resp -> continueProcessing(processor, resp, responseCb));
		return future;
	}
	
	public CompletableFuture<Void> invokeHtmlController(MatchResult result, RequestContext requestCtx, ResponseStreamer responseCb) {
		RouteMeta meta = result.getMeta();
		Service<MethodMeta, Action> service = meta.getService222();
		
		if(portConfig == null)
			portConfig = config.getPortConfigCallback().fetchPortConfig();
		ResponseProcessor processor = new ResponseProcessor(requestCtx, reverseRoutes, reverseTranslator, meta, responseCb, portConfig);
		
		Object obj = meta.getControllerInstance();
		if(obj == null)
			throw new IllegalStateException("Someone screwed up, as controllerInstance should not be null at this point, bug");
		Method method = meta.getMethod();

		if(service == null)
			throw new IllegalStateException("Bug, service should never be null at this point");
		
		Messages messages = new Messages(meta.getI18nBundleName(), "webpieces");
		requestCtx.setMessages(messages);

		RequestLocalCtx.set(processor);
		Current.setContext(requestCtx);
		CompletableFuture<Action> response;
		try {
			response = invokeHtmlMethod(service, obj, method, meta);
		} finally {
			RequestLocalCtx.set(null);
			Current.setContext(null);
		}
		
		CompletableFuture<Void> future = response.thenCompose(resp -> continueProcessing(processor, resp, responseCb));
		return future;
	}
	
	public  Service<MethodMeta, Action> createNotFoundService(RouteMeta m, RouterRequest req) {
		List<FilterInfo<?>> filterInfos = findNotFoundFilters(m.getFilters(), req.relativePath, req.isHttps);
		return controllerFinder.createNotFoundService(m, filterInfos);
	}

	public List<FilterInfo<?>> findNotFoundFilters(List<FilterInfo<?>> notFoundFilters, String path, boolean isHttps) {
		List<FilterInfo<?>> matchingFilters = new ArrayList<>();
		for(FilterInfo<?> info : notFoundFilters) {
			if(!info.securityMatch(isHttps))
				continue; //skip this filter
			
			matchingFilters.add(0, info);
		}
		return matchingFilters;
	}
	
	private CompletableFuture<Action> invokeContentMethod(Service<MethodMeta, Action> service, Object obj, Method m, RouteMeta meta) {
		RouteInfoForContent routeInfo = new RouteInfoForContent(meta.getBodyContentBinder());
		MethodMeta methodMeta = new MethodMeta(new LoadedController2(obj, m), Current.getContext(), routeInfo);
		return service.invoke(methodMeta);
	}
	
	private CompletableFuture<Action> invokeHtmlMethod(Service<MethodMeta, Action> service, Object obj, Method m, RouteMeta meta) {
		RouteInfoGeneric genericRouteInfo = new RouteInfoGeneric(meta.getRoute().isCheckSecureToken());
		MethodMeta methodMeta = new MethodMeta(new LoadedController2(obj, m), Current.getContext(), genericRouteInfo);
		return service.invoke(methodMeta);
	}

	private CompletableFuture<Action> invokeNotFoundMethod(Service<MethodMeta, Action> service, Object obj, Method m, RouteMeta meta) {
		RouteInfoForNotFound notFoundInfo = new RouteInfoForNotFound();
		MethodMeta methodMeta = new MethodMeta(new LoadedController2(obj, m), Current.getContext(), notFoundInfo);
		return service.invoke(methodMeta);
	}
	
	public CompletableFuture<Void> continueProcessing(ResponseProcessor processor, Action controllerResponse, ResponseStreamer responseCb) {
		if(controllerResponse instanceof RedirectImpl) {
			return processor.createFullRedirect((RedirectImpl)controllerResponse);
		} else if(controllerResponse instanceof AjaxRedirectImpl) {
			return processor.createAjaxRedirect((AjaxRedirectImpl)controllerResponse);
		} else if(controllerResponse instanceof RenderImpl) {
			return processor.createRenderResponse((RenderImpl)controllerResponse);
		} else if(controllerResponse instanceof RawRedirect) {
			//redirects to a raw straight up url
			return processor.createRawRedirect((RawRedirect)controllerResponse);
		} else if(controllerResponse instanceof RenderContent) {
			//Rendering stuff like json/protobuf/thrift or basically BodyContentBinder generated content
			return processor.createContentResponse((RenderContent)controllerResponse);
		} else {
			throw new UnsupportedOperationException("Bug, a webpieces developer must have missed some code to write");
		}
	}
	
	public void init(ReverseRoutes reverseRoutes) {
		this.reverseRoutes = reverseRoutes;
	}

}
