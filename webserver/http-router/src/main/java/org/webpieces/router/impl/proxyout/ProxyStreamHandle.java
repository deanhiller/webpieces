package org.webpieces.router.impl.proxyout;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.RouterStreamHandle;
import org.webpieces.router.impl.dto.RedirectResponse;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

public class ProxyStreamHandle implements RouterStreamHandle {
	private static final Logger log = LoggerFactory.getLogger(ProxyStreamHandle.class);

	private CompressionChunkingHandle handle;
	private ResponseCreator responseCreator;

	private ChannelCloser channelCloser;

	@Inject
    public ProxyStreamHandle(
    	CompressionChunkingHandle handle,
    	ResponseCreator responseCreator,
    	ChannelCloser channelCloser
    ) {
		this.handle = handle;
		this.responseCreator = responseCreator;
		this.channelCloser = channelCloser;
    }

	public void setRouterRequest(RouterRequest routerRequest) {
		handle.setRouterRequest(routerRequest);
	}

	public void init(RouterStreamHandle originalHandle) {
		handle.init(originalHandle);
	}
	
	public void turnCompressionOff() {
		handle.turnCompressionOff();
	}
	
    public CompletableFuture<StreamWriter> sendRedirectAndClearCookie(RouterRequest req, String badCookieName) {
        RedirectResponse httpResponse = new RedirectResponse(false, req.isHttps, req.domain, req.port, req.relativePath);
        Http2Response response = responseCreator.createRedirect(req.orginalRequest, httpResponse);

        responseCreator.addDeleteCookie(response, badCookieName);

        log.info("sending REDIRECT(due to bad cookie) response responseSender="+ this);
        CompletableFuture<StreamWriter> future = process(response);

        closeIfNeeded(req.orginalRequest);

        return future.thenApply(s -> null);
    }
    
	public Void closeIfNeeded(Http2Headers request) {
		String connHeader = request.getSingleHeaderValue(Http2HeaderName.CONNECTION);
		boolean close = false;
		if(!"keep-alive".equals(connHeader)) {
			close = true;
		} else
			close = false;
		
		if(close)
			closeIfNeeded();
		
		return null;
	}
	
    @Override
    public CompletableFuture<StreamWriter> process(Http2Response response) {
    	return handle.process(response);
    }

    @Override
    public Object getSocket() {
        return handle.getSocket();
    }

    @Override
    public Map<String, Object> getSession() {
        return handle.getSession();
    }

    @Override
    public boolean requestCameFromHttpsSocket() {
        return handle.requestCameFromHttpsSocket();
    }

    @Override
    public boolean requestCameFromBackendSocket() {
        return handle.requestCameFromBackendSocket();
    }

    @Deprecated
    @Override
    public Void closeIfNeeded() {
        return handle.closeIfNeeded();
    }

    @Override
    public PushStreamHandle openPushStream() {
        return handle.openPushStream();
    }

    @Override
    public CompletableFuture<Void> cancel(CancelReason payload) {
        return handle.cancel(payload);
    }

	public boolean hasSentResponseAlready() {
		return handle.hasSentResponseAlready();
	}



}
