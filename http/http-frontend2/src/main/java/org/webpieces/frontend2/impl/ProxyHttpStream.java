package org.webpieces.frontend2.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend2.api.HttpStream;
import org.webpieces.frontend2.api.ResponseStream;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
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
	public CompletableFuture<StreamWriter> incomingRequest(Http2Request request, ResponseStream stream) {
		String expect = request.getSingleHeaderValue("Expect");
		CompletableFuture<StreamWriter> future = CompletableFuture.completedFuture(null); 
		if(expect != null && "100-continue".equals(expect.toLowerCase())) {
			Http2Response continueResponse = new Http2Response();
			continueResponse.setEndOfStream(false);
			continueResponse.addHeader(new Http2Header(Http2HeaderName.STATUS, "100"));
			
			future = stream.sendResponse(continueResponse);
		}
		
		return future.thenCompose(s -> openStream.incomingRequest(request, stream));
	}

	@Override
	public CompletableFuture<Void> incomingCancel(CancelReason payload) {
		return openStream.incomingCancel(payload);
	}

}
