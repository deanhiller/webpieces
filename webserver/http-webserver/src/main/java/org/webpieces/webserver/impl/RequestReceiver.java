package org.webpieces.webserver.impl;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.webpieces.frontend2.api.FrontendStream;
import org.webpieces.frontend2.api.HttpRequestListener;
import org.webpieces.frontend2.api.SocketInfo;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.RstStreamFrame;

public class RequestReceiver implements HttpRequestListener {
	
	//private static final Logger log = LoggerFactory.getLogger(RequestReceiver.class);
	
	@Inject
	private RequestHelpFacade facade;
	
	@Override
	public StreamWriter incomingRequest(FrontendStream stream, Http2Headers headers, SocketInfo info) {
		RequestStreamWriter writer = new RequestStreamWriter(facade, stream, headers, info);
		stream.getSession().put("writer", writer);
		
		if(headers.isEndOfStream()) {
			CompletableFuture<Void> future = writer.handleCompleteRequest();
			writer.setOutstandingRequest(future);
			return writer;
		}

		return writer;
	}

	@Override
	public void cancelRequest(FrontendStream stream, RstStreamFrame c) {
		RequestStreamWriter writer = (RequestStreamWriter) stream.getSession().get("writer");
		writer.cancelOutstandingRequest();
	}

}
