package org.webpieces.webserver.impl;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.webpieces.http2parser.api.dto.CancelReason;
import org.webpieces.frontend2.api.ResponseStream;
import org.webpieces.frontend2.impl.ProtocolType;
import org.webpieces.router.api.RouterResponseHandler;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.StreamRef;
import com.webpieces.http2engine.api.StreamWriter;

public class RouterResponseHandlerImpl implements RouterResponseHandler {
	
    private ResponseStream stream;

    public RouterResponseHandlerImpl(ResponseStream stream) {
        this.stream = stream;
    }

    @Override
    public StreamRef process(Http2Response response) {
        return stream.process(response);
    }

    @Override
    public PushStreamHandle openPushStream() {
        return stream.openPushStream();
    }

    @Override
    public Object getSocket() {
        return stream.getSocket();
    }

    @Override
    public Map<String, Object> getSession() {
        return stream.getSession();
    }

	@Override
	public boolean requestCameFromHttpsSocket() {
		return stream.getSocket().isForServingHttpsPages();
	}

	@Override
	public boolean requestCameFromBackendSocket() {
		return stream.getSocket().isBackendSocket();
	}

	@Override
	public Void closeIfNeeded() {
		if(stream.getSocket().getProtocol() == ProtocolType.HTTP1_1)
			stream.getSocket().close("Connection KeepAlive not set");
		return null;
	}


}
