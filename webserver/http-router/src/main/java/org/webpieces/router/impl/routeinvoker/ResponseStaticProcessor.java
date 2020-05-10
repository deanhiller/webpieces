package org.webpieces.router.impl.routeinvoker;

import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.impl.dto.RenderStaticResponse;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;

public class ResponseStaticProcessor {

	private RequestContext ctx;
	private ResponseStreamer responseCb;
	private ProxyStreamHandle handler;
	
	public ResponseStaticProcessor(RequestContext ctx, ResponseStreamer responseCb, ProxyStreamHandle handler) {
		this.ctx = ctx;
		this.responseCb = responseCb;
		this.handler = handler;
		
	}
	public CompletableFuture<Void> renderStaticResponse(RenderStaticResponse renderStatic) {
		boolean wasSet = Current.isContextSet();
		if(!wasSet)
			Current.setContext(ctx); //Allow html tags to use the contexts
		try {
			return responseCb.sendRenderStatic(renderStatic, handler);
		} finally {
			if(!wasSet) //then reset
				Current.setContext(null);
		}
	}
	
}
