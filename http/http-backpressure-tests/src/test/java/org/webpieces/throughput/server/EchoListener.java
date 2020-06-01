package org.webpieces.throughput.server;

import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.HttpStream;
import org.webpieces.frontend2.api.ResponseStream;
import org.webpieces.frontend2.api.StreamListener;
import org.webpieces.throughput.RequestCreator;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.StreamRef;

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
			return stream.process(resp);
		}

	}

	@Override
	public void fireIsClosed(FrontendSocket socketThatClosed) {
	}
}
