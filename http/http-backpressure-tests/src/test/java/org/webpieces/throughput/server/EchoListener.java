package org.webpieces.throughput.server;

import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.HttpStream;
import org.webpieces.frontend2.api.ResponseStream;
import org.webpieces.frontend2.api.StreamListener;
import org.webpieces.throughput.RequestCreator;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.StreamRef;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;

public class EchoListener implements StreamListener {

	@Override
	public HttpStream openStream(FrontendSocket socket) {
		return new EchoStream();
	}

	private class EchoStream implements HttpStream {
		@Override
		public StreamRef incomingRequest(Http2Request request, ResponseStream stream) {
			Http2Response resp = RequestCreator.createHttp2Response(request.getStreamId());
			
			//automatically transfers backpressure back to the writer so if the reader of responses(the client in this case) 
			//slows down, the writer(the client as well in this case) also is forced to slow down sending requests
			CompletableFuture<StreamWriter> responseWriter = stream.process(resp);
			
			return new MyStreamRef(stream, responseWriter);
		}

	}

	private class MyStreamRef implements StreamRef {

		private ResponseStream stream;
		private CompletableFuture<StreamWriter> responseWriter;

		public MyStreamRef(ResponseStream stream, CompletableFuture<StreamWriter> responseWriter) {
			this.stream = stream;
			this.responseWriter = responseWriter;
		}

		@Override
		public CompletableFuture<StreamWriter> getWriter() {
			return responseWriter;
		}

		@Override
		public CompletableFuture<Void> cancel(CancelReason reason) {
			//probably redundant to cancel the response stream since request stream is already cancelling it!!
			return stream.cancel(reason);
		}
		
	}
	
	@Override
	public void fireIsClosed(FrontendSocket socketThatClosed) {
	}
}
