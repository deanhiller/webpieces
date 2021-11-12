package org.webpieces.router.impl.routeinvoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.Messages;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.exceptions.ControllerException;
import org.webpieces.util.exceptions.WebpiecesException;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.ReverseRoutes;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routers.Endpoint;
import org.webpieces.util.futures.FutureHelper;

import com.webpieces.http2.api.streaming.StreamRef;

@Singleton
public class ServiceInvoker {

	protected FutureHelper futureUtil;
	private ReverseRoutes reverseRoutes;

	@Inject
	public ServiceInvoker(
			FutureHelper futureUtil
	) {
		this.futureUtil = futureUtil;
	}

	/**
	 * In DevelopmentServer, this is called on every recompile where routes have changed.  In prod, it's called once
	 */
	public void init(ReverseRoutes reverseRoutes) {
		this.reverseRoutes = reverseRoutes;
	}
	
	public XFuture<Void> invokeSvc(
			MethodMeta meta,
			String i18nBundleName,
			Endpoint service,
			Processor processor,
			ProxyStreamHandle handle
	) {
		if(processor == null || meta == null || service == null || handle == null)
			throw new IllegalArgumentException("nothing can be null into this metehod");
		
		handle.initJustBeforeInvoke(reverseRoutes, meta);

		RequestContext requestCtx = meta.getCtx();
		LoadedController loadedController = meta.getLoadedController();
		Messages messages = new Messages(i18nBundleName, "webpieces");
		requestCtx.setMessages(messages);

		XFuture<Action> response = futureUtil.catchBlockWrap(
			() -> invokeService(service, meta),
			(t) -> convert(loadedController, t)
		);

		return response.thenCompose(resp -> continueProcessing(handle, meta, resp, processor));
	}

	private XFuture<Void> continueProcessing(ProxyStreamHandle handle, MethodMeta meta, Action resp, Processor processor) {
		return processor.continueProcessing(meta, resp, handle);
	}
	
	
	public XFuture<Action> invokeService(Endpoint service, MethodMeta methodMeta) {
		return service.invoke(methodMeta);
	}
	
	private Throwable convert(LoadedController loadedController, Throwable t) {
		if(t instanceof WebpiecesException) {
			//MUST wrap with same exact exception so upper layers can detect and route
			WebpiecesException exc = (WebpiecesException) t;
			return exc.clone("exception occurred trying to invoke controller method(and filters)="+loadedController.getControllerMethod()); 
		}
		return new ControllerException("exception occurred on controller method="+loadedController.getControllerMethod(), t);
	}



	public StreamRef invokeStream(MethodMeta meta, Method m, Object instance, ProxyStreamHandle handle) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		handle.initJustBeforeInvoke(reverseRoutes, meta);

		return (StreamRef) m.invoke(instance, handle);
	}
}
