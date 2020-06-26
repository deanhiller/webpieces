package org.webpieces.webserver.test.http2.directfast;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

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
	public CompletableFuture<Void> connect(InetSocketAddress addr) {
		return CompletableFuture.completedFuture(null); //pretend we connected
	}

	@Override
	public CompletableFuture<FullResponse> send(FullRequest request) {
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
	public CompletableFuture<Void> close() {
		streamListener.fireIsClosed(frontendSocket);
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Void> sendPing() {
		throw new UnsupportedOperationException("not supported");
	}

}
