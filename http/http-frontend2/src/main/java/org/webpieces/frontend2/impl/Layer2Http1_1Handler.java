package org.webpieces.frontend2.impl;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend2.api.HttpStream;
import org.webpieces.frontend2.api.StreamListener;
import org.webpieces.http2translations.api.Http1_1ToHttp2;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.MarshalState;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.httpparser.api.dto.HttpMessageType;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.util.acking.AckAggregator;
import com.webpieces.util.acking.ByteAckTracker;
import com.webpieces.util.locking.PermitQueue;

public class Layer2Http1_1Handler {
	private static final Logger log = LoggerFactory.getLogger(Layer2Http1_1Handler.class);
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private HttpParser httpParser;
	private StreamListener httpListener;
	private AtomicInteger counter = new AtomicInteger(1);
	private ByteAckTracker ackTracker = new ByteAckTracker();
	
	public Layer2Http1_1Handler(HttpParser httpParser, StreamListener httpListener) {
		this.httpParser = httpParser;
		this.httpListener = httpListener;
	}

	public InitiationResult initialData(FrontendSocketImpl socket, ByteBuffer buf) {
			return initialDataImpl(socket, buf);
	}
	
	public InitiationResult initialDataImpl(FrontendSocketImpl socket, ByteBuffer buf) {
		
		Memento state = socket.getHttp1_1ParseState();
		int newDataSize = buf.remaining();
		int total = state.getLeftOverData().getReadableSize() + buf.remaining(); 
		state = parse(socket, buf);
		int numBytesRead = total - state.getLeftOverData().getReadableSize();
		
		//IF we are receiving a preface, there will ONLY be ONE message AND leftover data
		InitiationResult result = checkForPreface(socket, state);
		if(result != null)
			return result;

		//TODO: check for EXACTLY ONE http request AND check if it is an h2c header with Http-Settings header!!!!
		//if so, return that initiation result and start using the http2 code
		
		//if we get this far, we now know we are http1.1
		if(state.getParsedMessages().size() > 0) {
			processWithBackpressure(socket, newDataSize, numBytesRead)
				.exceptionally(t -> logException(t));
			return new InitiationResult(InitiationStatus.HTTP1_1);
		} else {
			ackTracker.createTracker(newDataSize, 0, numBytesRead);
		}
		
		return null; // we don't know yet(not enough data)
	}
	
	private Void logException(Throwable t) {
		log.error("exception", t);
		return null;
	}

	private InitiationResult checkForPreface(FrontendSocketImpl socket, Memento state) {
		if(state.getParsedMessages().size() != 1)
			return null;
		if(state.getParsedMessages().get(0).getMessageType() != HttpMessageType.HTTP2_MARKER_MSG)
			return null;

		//release memory associated with 1.1 parser for this socket
		socket.setHttp1_1ParseState(null, null);
		
		return new InitiationResult(state.getLeftOverData(), InitiationStatus.PREFACE);
	}

	public CompletableFuture<Void> incomingData(FrontendSocketImpl socket, ByteBuffer buf) {
		Memento state = socket.getHttp1_1ParseState();
		int newDataSize = buf.remaining();
		int total = state.getLeftOverData().getReadableSize() + buf.remaining(); 
		state = parse(socket, buf);
		int numBytesRead = total - state.getLeftOverData().getReadableSize();
		
		return processWithBackpressure(socket, newDataSize, numBytesRead).exceptionally(t -> {
			log.error("Exception", t);
			socket.close("Exception so closing http1.1 socket="+t.getMessage());
			return null;
		});
	}
	
	public CompletableFuture<Void> processWithBackpressure(
			FrontendSocketImpl socket, int newDataSize, int numBytesRead) {
		Memento state = socket.getHttp1_1ParseState();
		List<HttpPayload> parsed = state.getParsedMessages();
		AckAggregator ack = ackTracker.createTracker(newDataSize, parsed.size(), numBytesRead);

		CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
		for(HttpPayload payload : parsed) {
			future = future.thenCompose(w -> {
				return processCorrectly(socket, payload);
			}).handle((v, t) -> ack.ack(v, t));
		}
		
		return ack.getAckBytePayloadFuture();
	}

	private Memento parse(FrontendSocketImpl socket, ByteBuffer buf) {
		DataWrapper moreData = dataGen.wrapByteBuffer(buf);
		Memento state = socket.getHttp1_1ParseState();
		state = httpParser.parse(state, moreData);
		return state;
	}
	
	private CompletableFuture<Void> processCorrectly(FrontendSocketImpl socket, HttpPayload payload) {
		Http2Msg msg = Http1_1ToHttp2.translate(payload, socket.isHttps());

		if(payload instanceof HttpRequest) {
			return processInitialPieceOfRequest(socket, (HttpRequest) payload, (Http2Request)msg);
		} else if(msg instanceof DataFrame) {
			return processData(socket, (DataFrame)msg);
		} else {
			throw new IllegalArgumentException("payload not supported="+payload);
		}
	}

	private CompletableFuture<Void> processData(FrontendSocketImpl socket, DataFrame msg) {
		PermitQueue permitQueue = socket.getPermitQueue();
		return permitQueue.runRequest(() -> {
			
			Http1_1StreamImpl stream = socket.getCurrentStream();
			StreamWriter requestWriter = stream.getRequestWriter();
			if(msg.isEndOfStream())
				stream.setSentFullRequest(true);

			return requestWriter.processPiece(msg).thenApply(v -> {
				stream.setRequestWriter(requestWriter);
				//since this is NOT end of stream, release permit for next data to come in
				if(!msg.isEndOfStream())
					permitQueue.releasePermit();
				//else we need to wait for FULL response to be sent back and then release 
				//the permit
				
				return null;
			});
		});

	}

	private CompletableFuture<Void> processInitialPieceOfRequest(FrontendSocketImpl socket, HttpRequest http1Req, Http2Request headers) {
		int id = counter.getAndAdd(2);
		
		PermitQueue permitQueue = socket.getPermitQueue();
		return permitQueue.runRequest(() -> {
			Http1_1StreamImpl currentStream = new Http1_1StreamImpl(id, socket, httpParser, permitQueue);

			HttpStream streamHandle = httpListener.openStream();
			currentStream.setStreamHandle(streamHandle);
			socket.setCurrentStream(currentStream);

			if(!headers.isEndOfStream()) {
				//in this case, we are NOT at the end of the request so we must let the next piece of
				//data run right after the request
				return streamHandle.incomingRequest(headers, currentStream).thenApply(w -> {
					currentStream.setRequestWriter(w);
					//must release the permit so the next data piece(which may be cached) can come in
					permitQueue.releasePermit();
					return null;
				});
			} else {
				//in this case, since this is the END of the request, we cannot release the permit in the
				//permit queue as we do not want to let the next request to start until the full response is
				//sent back to the client
				currentStream.setSentFullRequest(true);
				return streamHandle.incomingRequest(headers, currentStream).thenApply(w -> {
					currentStream.setRequestWriter(w);
					return null;
				});
			}
		});
	}

	public void socketOpened(FrontendSocketImpl socket, boolean isReadyForWrites) {
		Memento parseState = httpParser.prepareToParse();
		MarshalState marshalState = httpParser.prepareToMarshal();
		socket.setHttp1_1ParseState(parseState, marshalState);
		//timeoutListener.connectionOpened(socket, isReadyForWrites);
	}

	public void farEndClosed(FrontendSocketImpl socket) {
		socket.farEndClosed(httpListener);
	}

}
