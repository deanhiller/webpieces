package org.webpieces.router.impl.proxyout;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.RouterStreamHandle;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;

public class ProxyStreamHandle implements RouterStreamHandle {

	private CompressionChunkingHandle handle;

	@Inject
    public ProxyStreamHandle(
    	CompressionChunkingHandle handle
    ) {
		this.handle = handle;
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
