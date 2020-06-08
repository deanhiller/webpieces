package org.webpieces.webserver.impl;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.ResponseStream;
import org.webpieces.frontend2.impl.ProtocolType;
import org.webpieces.router.api.RouterResponseHandler;

import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.streaming.PushStreamHandle;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

public class RouterResponseHandlerImpl implements RouterResponseHandler {
	
    private ResponseStream stream;

    public RouterResponseHandlerImpl(ResponseStream stream) {
        this.stream = stream;
    }

    @Override
    public CompletableFuture<StreamWriter> process(Http2Response response) {
        return stream.process(response);
    }

	@Override
	public CompletableFuture<Void> cancel(CancelReason reason) {
		return stream.cancel(reason);
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

	/**
	 * Use cancel instead?
	 */
	@Deprecated 
	@Override
	public Void closeIfNeeded() {
		if(stream.getSocket().getProtocol() == ProtocolType.HTTP1_1)
			stream.getSocket().close("Connection KeepAlive not set");
		return null;
	}

	@Override
	public void closeSocket(String reason) {
		FrontendSocket socket = stream.getSocket();
		socket.close(reason);
	}


}
