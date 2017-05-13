package org.webpieces.frontend2.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend2.api.HttpRequestListener;
import org.webpieces.frontend2.api.Protocol;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.api.server.ResponseHandler;
import com.webpieces.http2engine.api.server.ServerEngineListener;
import com.webpieces.http2engine.api.server.StreamReference;
import com.webpieces.http2parser.api.Http2Exception;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

public class Layer3Http2EngineListener implements ServerEngineListener {

	private static final Logger log = LoggerFactory.getLogger(Layer3Http2EngineListener.class);
	
	private FrontendSocketImpl socket;
	private HttpRequestListener httpListener;
	private String scheme;

	public Layer3Http2EngineListener(FrontendSocketImpl socket, HttpRequestListener httpListener, boolean isHttps) {
		this.socket = socket;
		this.httpListener = httpListener;
		if(isHttps)
			scheme = "https";
		else
			scheme = "http";
	}

	@Override
	public StreamReference sendRequestToServer(Http2Headers request, ResponseHandler responseHandler) {
		
		Http2Header header = request.getHeaderLookupStruct().getHeader(Http2HeaderName.SCHEME);
		if(header != null) {
			verifyAndResetScheme(header);
		} else {
			request.addHeader(new Http2Header(Http2HeaderName.SCHEME, scheme));
		}
		
		//every request received is a new stream
		Http2StreamImpl stream = new Http2StreamImpl(socket, responseHandler);
		StreamWriter writer = httpListener.incomingRequest(stream, request, Protocol.HTTP2);
		return new StreamRefImpl(stream, httpListener, writer);
	}

	private void verifyAndResetScheme(Http2Header header) {
		String value = header.getValue();
		if(!scheme.equals(value))
			log.error("incoming request says it is scheme="+value+" but it is actually coming from port="+scheme+" so we are overriding");

		header.setValue(scheme);
	}

	@Override
	public CompletableFuture<Void> sendToSocket(ByteBuffer newData) {
		return socket.getChannel().write(newData).thenApply(c -> null);
	}

	public void closeSocket(Http2Exception reason) {
		socket.internalClose();
	}

}
