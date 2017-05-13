package org.webpieces.frontend2.impl;

import java.nio.ByteBuffer;
import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend2.api.HttpRequestListener;
import org.webpieces.frontend2.api.Protocol;
import org.webpieces.frontend2.impl.translation.Http2Translations;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.httpparser.api.dto.HttpChunk;
import org.webpieces.httpparser.api.dto.HttpLastChunk;
import org.webpieces.httpparser.api.dto.HttpMessageType;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class Layer2Http1_1Handler {
	private static final Logger log = LoggerFactory.getLogger(Layer2Http1_1Handler.class);
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private HttpParser httpParser;
	private HttpRequestListener httpListener;
	private boolean isHttps;

	public Layer2Http1_1Handler(HttpParser httpParser, HttpRequestListener httpListener, boolean isHttps) {
		this.httpParser = httpParser;
		this.httpListener = httpListener;
		this.isHttps = isHttps;
	}

	public InitiationResult initialData(FrontendSocketImpl socket, ByteBuffer buf) {
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
			processInitialPieceOfRequest(socket, payload, msg);
		} else if(payload instanceof HttpChunk) {
			processChunk(socket, (HttpChunk)payload, (DataFrame) msg);
			
		} else {
			throw new IllegalArgumentException("payload not supported="+payload);
		}
	}

	private void processChunk(FrontendSocketImpl socket, HttpChunk payload, DataFrame data) {
		StreamWriter writer = socket.getWriter();
		writer.send(data);
	}

	private void processInitialPieceOfRequest(FrontendSocketImpl socket, HttpPayload payload, Http2Msg msg) {
		HttpRequest http1Req = (HttpRequest) payload;
		Http2Headers headers = (Http2Headers) msg;
		
		Http1_1StreamImpl stream = new Http1_1StreamImpl(socket, httpParser);
		//socket.setActiveHttp11Stream(stream);
					
		Http2Header lengthHeader = headers.getHeaderLookupStruct().getHeader(Http2HeaderName.CONTENT_LENGTH);
		if(lengthHeader != null) {
			DataFrame frame = Http2Translations.translateBody(payload.getBody());
			socket.setSendRequestState(RequestState.SENT_REQUEST);
			
			StreamWriter writer = httpListener.incomingRequest(stream, headers, Protocol.HTTP11);
			writer.send(frame);
			
		} else if(http1Req.isHasChunkedTransferHeader()) {
			StreamWriter writer = httpListener.incomingRequest(stream, headers, Protocol.HTTP11);
			socket.addWriter(writer);
		} else {
			socket.setSendRequestState(RequestState.SENT_REQUEST);
			httpListener.incomingRequest(stream, headers, Protocol.HTTP11);
		}
	}

	public void socketOpened(FrontendSocketImpl socket, boolean isReadyForWrites) {
		Memento parseState = httpParser.prepareToParse();
		socket.setHttp1_1ParseState(parseState);
		//timeoutListener.connectionOpened(socket, isReadyForWrites);
	}

}
