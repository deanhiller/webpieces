package org.webpieces.router.impl.routebldr;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.streams.StreamService;
import org.webpieces.router.impl.body.BodyParsers;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routeinvoker.NullWriter;
import org.webpieces.router.impl.routeinvoker.Processor;
import org.webpieces.router.impl.routeinvoker.RequestStreamWriter2;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;
import org.webpieces.router.impl.routeinvoker.ServiceInvoker;
import org.webpieces.router.impl.routers.Endpoint;

import com.webpieces.http2.api.streaming.StreamWriter;

public class RequestResponseStream implements StreamService {

	private BodyParsers requestBodyParsers;

	private Endpoint service;
	private Processor processor;
	private ServiceInvoker invoker;
	private String i18nBundleName;
	
	public RequestResponseStream(
			Endpoint service, 
			String i18nBundleName,
			Processor processor, 			
			BodyParsers bodyParsers,
			ServiceInvoker invoker
	) {
		this.service = service;
		this.i18nBundleName = i18nBundleName;
		this.processor = processor;
		requestBodyParsers = bodyParsers;
		this.invoker = invoker;
	}

	@Override
	public RouterStreamRef openStream(MethodMeta meta, ProxyStreamHandle handle) {
		
		boolean endOfStream = meta.getCtx().getRequest().originalRequest.isEndOfStream();
		if(endOfStream) {
			//If there is no body, just invoke to process OR IN CASE of InternalError or NotFound, there is NO need
			//to wait for the request body and we can respond early, which stops wasting CPU of reading in their body
			meta.getCtx().getRequest().body = DataWrapperGeneratorFactory.EMPTY;
			CompletableFuture<StreamWriter> invokeSvc = invoker.invokeSvc(meta, i18nBundleName, service, processor, handle)
																.thenApply(voidd -> new NullWriter());
			return new RouterStreamRef("reqRespStreamProxy", invokeSvc, null);
		}
		
		//At this point, we don't have the end of the stream so return a request writer that calls invoke when complete
		RequestStreamWriter2 writer = new RequestStreamWriter2(requestBodyParsers, meta,
				(newInfo) -> invoker.invokeSvc(newInfo, i18nBundleName, service, processor, handle)
		);
		
		CompletableFuture<StreamWriter> w = CompletableFuture.completedFuture(writer);
		return new RouterStreamRef("requestRespStream", w, null);
	}

}
