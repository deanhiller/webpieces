package org.webpieces.webserver.test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpclient11.api.HttpDataWriter;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpResponseListener;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.MarshalState;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.webserver.test.http11.Http11SocketImpl;

public class Http11Socket {

	private HttpSocket socket;

	public Http11Socket(HttpSocket socket) {
		this.socket = socket;
	}

	public CompletableFuture<HttpFullResponse> send(HttpFullRequest req) {
		return socket.send(req);
	}
	
	public void send(HttpPayload payload) {
		socket.send(request, l)
		
		ByteBuffer buf = parser.marshalToByteBuffer(state, payload);
		dataListener.incomingData(channel, buf);
	}
	
	public CompletableFuture<Void> close() {
		return socket.close();
	}

	public List<FullResponse> getResponses() {
		return channel.getResponses();
	}

	public CompletableFuture<Void> sendBytes(DataWrapper dataWrapper) {
//		Http11SocketImpl impl = (Http11SocketImpl) socket;
//		return impl.sendBytes(dataWrapper);
	}



}
