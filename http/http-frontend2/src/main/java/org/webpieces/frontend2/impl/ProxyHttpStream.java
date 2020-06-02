package org.webpieces.frontend2.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend2.api.HttpStream;
import org.webpieces.frontend2.api.ResponseStream;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.StreamRef;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

public class ProxyHttpStream implements HttpStream {

	private HttpStream openStream;

	public ProxyHttpStream(HttpStream openStream) {
		this.openStream = openStream;
	}

	@Override
	public StreamRef incomingRequest(Http2Request request, ResponseStream stream) {
		String expect = request.getSingleHeaderValue("Expect");
		CompletableFuture<StreamWriter> future = CompletableFuture.completedFuture(null); 
		if(expect != null && "100-continue".equals(expect.toLowerCase())) {
			Http2Response continueResponse = new Http2Response();
			continueResponse.setEndOfStream(false);
			continueResponse.addHeader(new Http2Header(Http2HeaderName.STATUS, "100"));
			
			future = stream.process(continueResponse);
		}
		
		StreamRef streamRef = openStream.incomingRequest(request, stream);

		CompletableFuture<StreamWriter> writer = future.thenCompose(w -> streamRef.getWriter());
		return new ProxyStreamRef(writer, streamRef);
	}

	private class ProxyStreamRef implements StreamRef {
		private CompletableFuture<StreamWriter> writer;
		private StreamRef streamRef;

		public ProxyStreamRef(CompletableFuture<StreamWriter> writer, StreamRef streamRef) {
			this.writer = writer;
			this.streamRef = streamRef;
		}

		@Override
		public CompletableFuture<StreamWriter> getWriter() {
			return writer;
		}

		@Override
		public CompletableFuture<Void> cancel(CancelReason reason) {
			return streamRef.cancel(reason);
		}
	}
	
}
