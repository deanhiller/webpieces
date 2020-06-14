package org.webpieces.router.impl.routebldr;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.exceptions.ControllerException;
import org.webpieces.router.api.exceptions.WebpiecesException;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.streams.StreamService;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;
import org.webpieces.router.impl.routeinvoker.ServiceInvoker;
import org.webpieces.util.futures.FutureHelper;

import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

public class StreamProxy implements StreamService {

	private FutureHelper futureUtil;
	private ServiceInvoker serviceInvoker;
	
	public StreamProxy(FutureHelper futureUtil, ServiceInvoker serviceInvoker) {
		this.futureUtil = futureUtil;
		this.serviceInvoker = serviceInvoker;
	}

	@Override
	public RouterStreamRef openStream(MethodMeta meta, ProxyStreamHandle handle) {
		RequestContext requestCtx = meta.getCtx();
		LoadedController loadedController = meta.getLoadedController();
		Object instance = loadedController.getControllerInstance();
		Method controllerMethod = loadedController.getControllerMethod();
		Parameter[] parameters = loadedController.getParameters();
		
		if(parameters.length != 1)
			throw new IllegalArgumentException("Your method='"+controllerMethod+"' MUST one parameter and does not.  It needs to take a RouterStreamHandler");
		else if(!ResponseStreamHandle.class.equals(parameters[0].getType()))
			throw new IllegalArgumentException("The single parameter must be RouterStreamHandle and was not for this method='"+controllerMethod+"'");
		else if(!StreamRef.class.equals(controllerMethod.getReturnType()))
			throw new IllegalArgumentException("The return value must be a subclass of StreamRef and was not for this method='"+controllerMethod+"'");

		
		StreamRef streamRef = invokeStream(meta, controllerMethod, instance, requestCtx, handle);
		CompletableFuture<StreamWriter> writer = streamRef.getWriter();
		
		CompletableFuture<StreamWriter> newFuture = futureUtil.catchBlockWrap(
				() -> writer,
				(t) -> convert(loadedController, t)
		); 

		Function<CancelReason, CompletableFuture<Void>> cancelFunc = (reason) -> streamRef.cancel(reason);		
		return new RouterStreamRef("streamProxy", newFuture, cancelFunc);
	}

	private StreamRef invokeStream(MethodMeta meta, Method m, Object instance, RequestContext requestCtx, ProxyStreamHandle handle) {
		try {
			
			StreamRef streamRef = serviceInvoker.invokeStream(meta, m, instance, handle);
			if(streamRef == null) {
				throw new IllegalStateException("You must return a non-null and did not from method='"+m+"'");
			}
			return streamRef;
			
		} catch (Throwable e) {
			CompletableFuture<StreamWriter> failedFuture = futureUtil.failedFuture(e);
			return new RouterStreamRef("controllerFailed", failedFuture, null);
		}
	}
	
	private Throwable convert(LoadedController loadedController, Throwable t) {
		if(t instanceof WebpiecesException) {
			//MUST wrap with same exact exception so upper layers can detect and route
			WebpiecesException exc = (WebpiecesException) t;
			return exc.clone("exception occurred trying to invoke controller method(and filters)="+loadedController.getControllerMethod()); 
		}
		return new ControllerException("exception occurred on controller method="+loadedController.getControllerMethod(), t);
	}
}
