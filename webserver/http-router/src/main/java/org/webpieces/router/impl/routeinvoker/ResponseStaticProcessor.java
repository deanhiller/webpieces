package org.webpieces.router.impl.routeinvoker;

import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.impl.dto.RenderStaticResponse;

public class ResponseStaticProcessor {

	private RequestContext ctx;
	private ResponseStreamer responseCb;
	
	public ResponseStaticProcessor(RequestContext ctx, ResponseStreamer responseCb) {
		this.ctx = ctx;
		this.responseCb = responseCb;
		
	}
	public CompletableFuture<Void> renderStaticResponse(RenderStaticResponse renderStatic) {
		boolean wasSet = Current.isContextSet();
		if(!wasSet)
			Current.setContext(ctx); //Allow html tags to use the contexts
		try {
			return responseCb.sendRenderStatic(renderStatic);
		} finally {
			if(!wasSet) //then reset
				Current.setContext(null);
		}
	}
	
}
