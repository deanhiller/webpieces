package org.webpieces.throughput.server;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.webpieces.frontend2.api.HttpStream;
import org.webpieces.frontend2.api.ResponseStream;
import org.webpieces.frontend2.api.StreamListener;
import org.webpieces.throughput.RequestCreator;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;

public class EchoListener implements StreamListener {

	private static final Logger log = LoggerFactory.getLogger(EchoListener.class);
	
	private AtomicInteger counter = new AtomicInteger();
	
	@Override
	public HttpStream openStream() {
		return new EchoStream();
	}

	private class EchoStream implements HttpStream {
		@Override
		public CompletableFuture<StreamWriter> incomingRequest(Http2Request request, ResponseStream stream) {
			Http2Response resp = RequestCreator.createHttp2Response(request.getStreamId());
			
//			int total = counter.incrementAndGet();
//			if(total % 2000 == 0) {
//				log.info("echoing response");
//			}
			
			//automatically transfers backpressure back to the writer so if the reader of responses(the client in this case) 
			//slows down, the writer(the client as well in this case) also is forced to slow down sending requests
			return stream.sendResponse(resp);
		}

		@Override
		public CompletableFuture<Void> incomingCancel(CancelReason payload) {
			log.error("This should not happen in this test. reason="+payload);
			return CompletableFuture.completedFuture(null);
		}
	}
}
