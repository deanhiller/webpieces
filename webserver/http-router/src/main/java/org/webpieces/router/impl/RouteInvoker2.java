package org.webpieces.router.impl;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.Messages;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.PortConfig;
import org.webpieces.router.api.PortConfigCallback;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.RenderContent;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.router.api.dto.RenderStaticResponse;
import org.webpieces.router.impl.actions.AjaxRedirectImpl;
import org.webpieces.router.impl.actions.RawRedirect;
import org.webpieces.router.impl.actions.RedirectImpl;
import org.webpieces.router.impl.actions.RenderImpl;
import org.webpieces.router.impl.ctx.RequestLocalCtx;
import org.webpieces.router.impl.ctx.ResponseProcessor;
import org.webpieces.router.impl.model.MatchResult;
import org.webpieces.router.impl.params.ObjectToParamTranslator;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.filters.Service;

public class RouteInvoker2 {

	private ReverseRoutes reverseRoutes;

	private ObjectToParamTranslator reverseTranslator;
	private PortConfigCallback portCallback;
	private volatile PortConfig portConfig;

	@Inject
	public RouteInvoker2(
		ObjectToParamTranslator reverseTranslator,
		RouterConfig config
	) {
		this.reverseTranslator = reverseTranslator;
		this.portCallback = config.getPortConfigCallback();
	}
	
	public CompletableFuture<Void> invokeStatic(MatchResult result, RequestContext ctx, ResponseStreamer responseCb) {
		RouteMeta meta = result.getMeta();

		StaticRoute route = (StaticRoute) meta.getRoute();
		boolean isOnClassPath = route.getIsOnClassPath();

		RenderStaticResponse resp = new RenderStaticResponse(route.getTargetCacheLocation(), isOnClassPath);
		if(route.isFile()) {
			resp.setFilePath(route.getFileSystemPath());
		} else {
			String relativeUrl = result.getPathParams().get("resource");
			VirtualFile fullPath = route.getFileSystemPath().child(relativeUrl);
			resp.setFileAndRelativePath(fullPath, relativeUrl);
		}

		ResponseProcessor processor = new ResponseProcessor(ctx, reverseRoutes, reverseTranslator, meta, responseCb, portConfig);

		return processor.renderStaticResponse(resp);
	}
	
	public CompletableFuture<Void> invokeController(MatchResult result, RequestContext requestCtx, ResponseStreamer responseCb) {
		
		if(portConfig == null)
			portConfig = portCallback.fetchPortConfig();
		RouteMeta meta = result.getMeta();
		ResponseProcessor processor = new ResponseProcessor(requestCtx, reverseRoutes, reverseTranslator, meta, responseCb, portConfig);
		
		Object obj = meta.getControllerInstance();
		if(obj == null)
			throw new IllegalStateException("Someone screwed up, as controllerInstance should not be null at this point, bug");
		Method method = meta.getMethod();

		if(meta.getService222() == null)
			throw new IllegalStateException("Bug, service should never be null at this point");
		
		Messages messages = new Messages(meta.getI18nBundleName(), "webpieces");
		requestCtx.setMessages(messages);

		RequestLocalCtx.set(processor);
		Current.setContext(requestCtx);
		CompletableFuture<Action> response;
		try {
			response = invokeMethod(obj, method, meta);
		} finally {
			RequestLocalCtx.set(null);
			Current.setContext(null);
		}
		
		CompletableFuture<Void> future = response.thenCompose(resp -> continueProcessing(processor, resp, responseCb));
		return future;
	}
	
	private CompletableFuture<Action> invokeMethod(Object obj, Method m, RouteMeta meta) {
		MethodMeta methodMeta = new MethodMeta(obj, m, Current.getContext(), meta.getRoute(), meta.getBodyContentBinder());
		Service<MethodMeta, Action> service = meta.getService222();
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
