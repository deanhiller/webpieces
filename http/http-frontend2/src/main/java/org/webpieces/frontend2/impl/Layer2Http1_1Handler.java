package org.webpieces.frontend2.impl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend2.api.HttpRequestListener;
import org.webpieces.frontend2.api.SocketInfo;
import org.webpieces.frontend2.impl.translation.Http2Translations;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.httpparser.api.dto.HttpChunk;
import org.webpieces.httpparser.api.dto.HttpMessageType;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.api.StreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class Layer2Http1_1Handler {
	private static final Logger log = LoggerFactory.getLogger(Layer2Http1_1Handler.class);
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private HttpParser httpParser;
	private HttpRequestListener httpListener;
	private boolean isHttps;
	private SocketInfo socketInfo;
	private AtomicInteger counter = new AtomicInteger(1);

	public Layer2Http1_1Handler(HttpParser httpParser, HttpRequestListener httpListener, boolean isHttps) {
		this.httpParser = httpParser;
		this.httpListener = httpListener;
		this.isHttps = isHttps;
		this.socketInfo = new SocketInfo(ProtocolType.HTTP1_1, isHttps);
	}

	public InitiationResult initialData(FrontendSocketImpl socket, ByteBuffer buf) {
			return initialDataImpl(socket, buf);

	}
	
	public InitiationResult initialDataImpl(FrontendSocketImpl socket, ByteBuffer buf) {
		Memento state = parse(socket, buf);
		
		//IF we are receiving a preface, there will ONLY be ONE message AND leftover data
		InitiationResult result = checkForPreface(socket, state);
		if(result != null)
			return result;

		//TODO: check for EXACTLY ONE http request AND check if it is an h2c header with Http-Settings header!!!!
		//if so, return that initiation result and start using the http2 code
		
		//if we get this far, we now know we are http1.1
		if(state.getParsedMessages().size() >= 0) {
			processHttp1Messages(socket, state);
			return new InitiationResult(InitiationStatus.HTTP1_1);
		}
		
		return null; // we don't know yet(not enough data)
	}
	
	private InitiationResult checkForPreface(FrontendSocketImpl socket, Memento state) {
		if(state.getParsedMessages().size() != 1)
			return null;
		if(state.getParsedMessages().get(0).getMessageType() != HttpMessageType.HTTP2_MARKER_MSG)
			return null;

		//release memory associated with 1.1 parser for this socket
		socket.setHttp1_1ParseState(null);
		
		return new InitiationResult(state.getLeftOverData(), InitiationStatus.PREFACE);
	}

	public void incomingData(FrontendSocketImpl socket, ByteBuffer buf) {
		Memento state = parse(socket, buf);
		processHttp1Messages(socket, state);
	}

	private Memento parse(FrontendSocketImpl socket, ByteBuffer buf) {
		DataWrapper moreData = dataGen.wrapByteBuffer(buf);
		Memento state = socket.getHttp1_1ParseState();
		state = httpParser.parse(state, moreData);
		return state;
	}

	private void processHttp1Messages(FrontendSocketImpl socket, Memento state) {
		List<HttpPayload> parsed = state.getParsedMessages();
		for(HttpPayload payload : parsed) {
			log.info("msg received="+payload);
			processCorrectly(socket, payload);
		}
	}

	private void processCorrectly(FrontendSocketImpl socket, HttpPayload payload) {
		Http2Msg msg = Http2Translations.translate(payload, isHttps);
		//TODO: close socket on violation of pipelining like previous request did not end

		if(payload instanceof HttpRequest) {
			processInitialPieceOfRequest(socket, (HttpRequest) payload, (Http2Request)msg);
		} else if(payload instanceof HttpChunk) {
			processChunk(socket, (HttpChunk)payload, (DataFrame) msg);
		} else {
			throw new IllegalArgumentException("payload not supported="+payload);
		}
	}

	private void processChunk(FrontendSocketImpl socket, HttpChunk payload, DataFrame data) {
		StreamWriter writer = socket.getWriter();
		writer.processPiece(data);
	}

	private CompletableFuture<StreamWriter> processInitialPieceOfRequest(FrontendSocketImpl socket, HttpRequest http1Req, Http2Request headers) {
		int id = counter.getAndAdd(2);
		Http1_1StreamImpl stream = new Http1_1StreamImpl(id, socket, httpParser);
		socket.setAddStream(stream);

		StreamHandle streamHandle = httpListener.openStream(stream, socketInfo);
		stream.setStreamHandle(streamHandle);
		
		String lengthHeader = headers.getSingleHeaderValue(Http2HeaderName.CONTENT_LENGTH);
		if(lengthHeader != null) {
			DataFrame frame = Http2Translations.translateBody(http1Req.getBody());
			CompletableFuture<StreamWriter> writer = streamHandle.process(headers);
			return writer.thenCompose( w -> w.processPiece(frame) );
		} else if(http1Req.isHasChunkedTransferHeader()) {
			CompletableFuture<StreamWriter> writer = streamHandle.process(headers);
			return writer.thenApply( w -> {
				socket.addWriter(w);
				return w;
			});
		} else {
			headers.setEndOfStream(true);
			return streamHandle.process(headers);
		}
	}

	public void socketOpened(FrontendSocketImpl socket, boolean isReadyForWrites) {
		Memento parseState = httpParser.prepareToParse();
		socket.setHttp1_1ParseState(parseState);
		//timeoutListener.connectionOpened(socket, isReadyForWrites);
	}

	public void farEndClosed(FrontendSocketImpl socket) {
		socket.farEndClosed(httpListener);
	}

	public void setBoundAddr(InetSocketAddress localAddr) {
		socketInfo.setBoundAddress(localAddr);
	}

}
