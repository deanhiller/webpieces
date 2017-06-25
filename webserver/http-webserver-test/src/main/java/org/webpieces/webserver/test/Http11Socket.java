package org.webpieces.webserver.test;

import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpparser.api.dto.HttpRequest;

public class Http11Socket {

	public void send(HttpRequest req) {
		// TODO Auto-generated method stub
		
	}

	public List<FullResponse> getResponses() {
		// TODO Auto-generated method stub
		return null;
	}

	public void send(HttpFullRequest req) {
		// TODO Auto-generated method stub
		
	}

	public void sendBytes(DataWrapper dataWrapper) {
		// TODO Auto-generated method stub
		
	}

	public void clear() {
		// TODO Auto-generated method stub
		
	}

//	private HttpSocket socket;
//
//	public Http11Socket(HttpSocket socket) {
//		this.socket = socket;
//	}
//
//	public CompletableFuture<HttpFullResponse> send(HttpFullRequest req) {
//		return socket.send(req);
//	}
//	
//	public void send(HttpPayload payload) {
//		socket.send(request, l)
//		
//		ByteBuffer buf = parser.marshalToByteBuffer(state, payload);
//		dataListener.incomingData(channel, buf);
//	}
//	
//	public CompletableFuture<Void> close() {
//		return socket.close();
//	}
//
//	public List<FullResponse> getResponses() {
//		return channel.getResponses();
//	}
//
//	public CompletableFuture<Void> sendBytes(DataWrapper dataWrapper) {
////		Http11SocketImpl impl = (Http11SocketImpl) socket;
////		return impl.sendBytes(dataWrapper);
//	}



}
