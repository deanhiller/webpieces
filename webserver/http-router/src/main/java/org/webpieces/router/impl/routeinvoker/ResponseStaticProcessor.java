package org.webpieces.router.impl.routeinvoker;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.BufferPool;
import org.webpieces.router.api.exceptions.WebSocketClosedException;
import org.webpieces.router.impl.dto.RenderStaticResponse;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.proxyout.filereaders.RequestInfo;
import org.webpieces.router.impl.proxyout.filereaders.StaticFileReader;
import org.webpieces.util.exceptions.NioClosedChannelException;
import org.webpieces.util.futures.FutureHelper;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.streaming.StreamWriter;

public class ResponseStaticProcessor {
	private static final Logger log = LoggerFactory.getLogger(ResponseStaticProcessor.class);

	private RequestContext ctx;
	private ProxyStreamHandle handler;
	private RouterRequest routerRequest;
	private Http2Request request;
	private FutureHelper futureUtil;

	private StaticFileReader reader;

	private BufferPool pool;
	
	public ResponseStaticProcessor(
			StaticFileReader reader,
			BufferPool pool,
			FutureHelper futureUtil, 
			RequestContext ctx, 
			ProxyStreamHandle handler
	) {
		this.reader = reader;
		this.pool = pool;
		this.futureUtil = futureUtil;
		this.ctx = ctx;
		this.routerRequest = ctx.getRequest();
		this.request = routerRequest.originalRequest;
		this.handler = handler;
		
	}
	public RouterStreamRef renderStaticResponse(RenderStaticResponse renderStatic) {
		boolean wasSet = Current.isContextSet();
		if(!wasSet)
			Current.setContext(ctx); //Allow html tags to use the contexts
		try {
			if(log.isDebugEnabled())
				log.debug("Sending render static html response. req="+request);
			RequestInfo requestInfo = new RequestInfo(routerRequest, request, pool, handler);
			
			
			CompletableFuture<StreamWriter> writer = futureUtil.catchBlockWrap(
				() -> reader.sendRenderStatic(requestInfo, renderStatic, handler), 
				(t) -> convert(t)
			);
			
			//TODO(dhiller): if socket closed or request cancelled, we should implement cancel function to stop reading
			//the file an pushing it back...
			return new RouterStreamRef("staticRef", writer, null);
			
			//return responseCb.sendRenderStatic(renderStatic, handler);
		} finally {
			if(!wasSet) //then reset
				Current.setContext(null);
		}
	}
	
	//TODO(dhiller): copy paste during refactor.  try to get back to same code for all of them
	@Deprecated
	private Throwable convert(Throwable t) {
		if(t instanceof NioClosedChannelException)
			//router does not know about the nio layer but it knows about WebSocketClosedException
			//so throw this as a flag to it that it doesn't need to keep trying error pages
			return new WebSocketClosedException("Socket is already closed", t);
		else
			return t;
	}
}
