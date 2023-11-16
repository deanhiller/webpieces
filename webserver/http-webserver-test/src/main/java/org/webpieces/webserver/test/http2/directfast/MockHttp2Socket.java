package org.webpieces.webserver.test.http2.directfast;

import java.net.InetSocketAddress;

import org.webpieces.util.HostWithPort;
import org.webpieces.util.futures.XFuture;

import org.webpieces.frontend2.api.HttpStream;
import org.webpieces.frontend2.api.StreamListener;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.Http2SocketListener;
import org.webpieces.http2client.api.dto.FullRequest;
import org.webpieces.http2client.api.dto.FullResponse;
import org.webpieces.http2client.impl.ResponseCacher;

import com.webpieces.http2.api.streaming.RequestStreamHandle;

public class MockHttp2Socket implements Http2Socket {

	private StreamListener streamListener;
	private MockFrontendSocket frontendSocket;

	public MockHttp2Socket(Http2SocketListener closeListener, StreamListener streamListener, boolean isHttps) {
		this.streamListener = streamListener;
		frontendSocket = new MockFrontendSocket(isHttps, new ProxyClose(closeListener, this));
	}

	@Override
	public XFuture<Void> connect(HostWithPort addr) {
		return XFuture.completedFuture(null); //pretend we connected
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	@Override
	public XFuture<Void> connect(InetSocketAddress addr) {
		return XFuture.completedFuture(null); //pretend we connected
	}

	@Override
	public XFuture<FullResponse> send(FullRequest request) {
		return new ResponseCacher(() -> openStream()).run(request);
	}

	@Override
	public RequestStreamHandle openStream() {
		if(streamListener == null) {
			String protocol = "http";
			if(frontendSocket.isForServingHttpsPages())
				protocol = "https";
			throw new IllegalStateException("Your arguments on webpieces startup told us not to bind a server for protocol="+protocol);
		}
		
		HttpStream stream = streamListener.openStream(frontendSocket);
		return new ProxyRequestStreamHandle(stream, frontendSocket);
	}

	@Override
	public XFuture<Void> close() {
		streamListener.fireIsClosed(frontendSocket);
		return XFuture.completedFuture(null);
	}

	@Override
	public XFuture<Void> sendPing() {
		throw new UnsupportedOperationException("not supported");
	}

}
