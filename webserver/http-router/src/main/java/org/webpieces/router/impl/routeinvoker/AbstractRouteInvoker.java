package org.webpieces.router.impl.routeinvoker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Provider;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.Messages;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.exceptions.ControllerException;
import org.webpieces.router.api.exceptions.WebpiecesException;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.ReverseRoutes;
import org.webpieces.router.impl.body.BodyParsers;
import org.webpieces.router.impl.dto.RenderStaticResponse;
import org.webpieces.router.impl.loader.ControllerLoader;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routebldr.BaseRouteInfo;
import org.webpieces.router.impl.routebldr.FilterInfo;
import org.webpieces.router.impl.routers.DynamicInfo;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForStatic;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.FutureHelper;

import com.webpieces.http2engine.api.StreamWriter;

public abstract class AbstractRouteInvoker implements RouteInvoker {

	protected final ControllerLoader controllerFinder;
	
	protected ReverseRoutes reverseRoutes;
	protected FutureHelper futureUtil;
	protected Provider<ResponseStreamer> proxyProvider;
	private BodyParsers requestBodyParsers;

	public AbstractRouteInvoker(
			ControllerLoader controllerFinder,
			FutureHelper futureUtil,
			BodyParsers bodyParsers,
			Provider<ResponseStreamer> proxyProvider
	) {
		this.controllerFinder = controllerFinder;
		this.futureUtil = futureUtil;
		this.proxyProvider = proxyProvider;
		this.requestBodyParsers = bodyParsers;
	}

	@Override
	public void init(ReverseRoutes reverseRoutes) {
		this.reverseRoutes = reverseRoutes;
	}
	
	@Override
	public CompletableFuture<StreamWriter> invokeStatic(RequestContext ctx, ProxyStreamHandle handler, RouteInfoForStatic data) {
		
		boolean isOnClassPath = data.isOnClassPath();

		RenderStaticResponse resp = new RenderStaticResponse(data.getTargetCacheLocation(), isOnClassPath);

		//NOTE: Looking up resource pictures in localhost:8080/@documentation stopped working if we
		//did not use the data.isRouteAFile() and used the filesystem information
		//we do have a test for this now if you try to fix it
		if(data.isRouteAFile()) {
			resp.setFilePath(data.getFileSystemPath());
		} else {
			String relativeUrl = ctx.getPathParams().get("resource");
			VirtualFile fullPath = data.getFileSystemPath().child(relativeUrl);
			resp.setFileAndRelativePath(fullPath, relativeUrl);
		}

		ResponseStreamer proxyResponse = proxyProvider.get();
		proxyResponse.init(ctx.getRequest(), handler);
		ResponseStaticProcessor processor = new ResponseStaticProcessor(ctx, proxyResponse, handler);

		return processor.renderStaticResponse(resp).thenApply(s -> new NullWriter());
	}
	
	protected CompletableFuture<StreamWriter> invokeImpl(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data, Processor processor, boolean forceEndOfStream) {
		Service<MethodMeta, Action> service = dynamicInfo.getService();
		LoadedController loadedController = dynamicInfo.getLoadedController();
		return invokeAny(invokeInfo, loadedController, service, data, processor, forceEndOfStream);
	}

	private CompletableFuture<StreamWriter> invokeAny(
			InvokeInfo invokeInfo,
			LoadedController loadedController,
			Service<MethodMeta, Action> service,
			RouteData data,
			Processor processor,
			boolean forceEndOfStream) {
		boolean endOfStream = invokeInfo.getRequestCtx().getRequest().originalRequest.isEndOfStream();
		if(forceEndOfStream || endOfStream) {
			//If there is no body, just invoke to process
			invokeInfo.getRequestCtx().getRequest().body = DataWrapperGeneratorFactory.EMPTY;
			return invokeAnyImpl(invokeInfo, loadedController, service, data, processor).thenApply(voidd -> null);
		}

		//At this point, we don't have the end of the stream
		RequestStreamWriter2 writer = new RequestStreamWriter2(requestBodyParsers, invokeInfo,
				(newInfo) -> invokeAnyImpl(newInfo, loadedController, service, data, processor)
		);

		return CompletableFuture.completedFuture(writer);
	}

	private CompletableFuture<Void> invokeAnyImpl(
		InvokeInfo invokeInfo, 
		LoadedController loadedController, 
		Service<MethodMeta, Action> service,
		RouteData data, 
		Processor processor
	) {
		BaseRouteInfo route = invokeInfo.getRoute();
		RequestContext requestCtx = invokeInfo.getRequestCtx();
		ProxyStreamHandle handler = invokeInfo.getHandler();

		if(service == null)
			throw new IllegalStateException("Bug, service should never be null at this point");
		
		Messages messages = new Messages(route.getRouteModuleInfo().i18nBundleName, "webpieces");
		requestCtx.setMessages(messages);

		Current.setContext(requestCtx);
		
		MethodMeta methodMeta = new MethodMeta(loadedController, Current.getContext(), data);
		CompletableFuture<Action> response;
		try {
			response = futureUtil.catchBlockWrap( 
				() -> service.invoke(methodMeta),
				(t) -> convert(loadedController, t)	
			);
		} finally {
			Current.setContext(null);
		}

		ResponseStreamer responseCb = proxyProvider.get();
		responseCb.init(requestCtx.getRequest(), handler);
		return response.thenCompose(resp -> processor.continueProcessing(resp, responseCb));
	}
	
	private Throwable convert(LoadedController loadedController, Throwable t) {
		if(t instanceof WebpiecesException)
			return t; //no wrapping t so upper layers can detect
		return new ControllerException("exception occurred on controller method="+loadedController.getControllerMethod(), t);
	}

	@Override
	public CompletableFuture<StreamWriter> invokeNotFound(InvokeInfo invokeInfo, LoadedController loadedController, RouteData data) {
		BaseRouteInfo route = invokeInfo.getRoute();
		RequestContext requestCtx = invokeInfo.getRequestCtx();
		Service<MethodMeta, Action> service = createNotFoundService(route, requestCtx.getRequest());

		ResponseStreamer responseCb = proxyProvider.get();
		responseCb.init(requestCtx.getRequest(), invokeInfo.getHandler());

		ResponseProcessorNotFound processor = new ResponseProcessorNotFound(
				invokeInfo.getRequestCtx(), reverseRoutes, 
				loadedController, responseCb, invokeInfo.getHandler());
		return invokeAny(invokeInfo, loadedController, service, data, processor, false);
	}
	
	private  Service<MethodMeta, Action> createNotFoundService(BaseRouteInfo route, RouterRequest req) {
		List<FilterInfo<?>> filterInfos = findNotFoundFilters(route.getFilters(), req.relativePath, req.isHttps);
		return controllerFinder.loadFilters(route, filterInfos, false);
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
}
