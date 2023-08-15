package org.webpieces.webserver.impl;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.Throttle;
import org.webpieces.util.futures.XFuture;

import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.ResponseStream;
import org.webpieces.frontend2.impl.ProtocolType;
import org.webpieces.router.api.RouterResponseHandler;

import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.streaming.PushStreamHandle;
import com.webpieces.http2.api.streaming.StreamWriter;

public class RouterResponseHandlerImpl implements RouterResponseHandler {

	private static final Logger log = LoggerFactory.getLogger(RouterResponseHandlerImpl.class);

	public static final Logger THROTTLE_LOGGER = Throttle.LOG;

    private ResponseStream stream;
	private Throttle throttler;

	private AtomicInteger count = new AtomicInteger();

	public RouterResponseHandlerImpl(ResponseStream stream, Throttle throttler) {
        this.stream = stream;
		this.throttler = throttler;
	}

    @Override
    public XFuture<StreamWriter> process(Http2Response response) {
        return stream.process(response).thenApply((str) -> {
			boolean decremented = false;
			if(response.isEndOfStream()) {
				throttler.decrement();

				if(THROTTLE_LOGGER.isDebugEnabled()) {
					int i = count.addAndGet(1);
					if (i % 10 == 0) {
						THROTTLE_LOGGER.debug("Response Headers EOM=" + i);
					}
				}
				decremented = true;
			}
			return new ThrottleProxy(str, throttler, decremented);
		});
    }

	@Override
	public XFuture<Void> cancel(CancelReason reason) {
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
