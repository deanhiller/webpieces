package org.webpieces.httpclient11.impl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpclient11.api.DataWriter;
import org.webpieces.httpclient11.api.HttpDataWriter;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpResponseListener;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.MarshalState;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.handlers.RecordingDataListener;
import org.webpieces.util.acking.AckAggregator;
import org.webpieces.util.acking.ByteAckTracker;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class HttpSocketImpl implements HttpSocket {

	private static final Logger log = LoggerFactory.getLogger(HttpSocketImpl.class);
	private static DataWrapperGenerator wrapperGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	private TCPChannel channel;

	private boolean isClosed;
	private boolean connected;
	
	private HttpParser parser;
	private Memento memento;
	private ConcurrentLinkedQueue<HttpResponseListener> responsesToComplete = new ConcurrentLinkedQueue<>();
	private DataListener dataListener = new MyDataListener();
	private boolean isRecording = false;
	private MarshalState state;
	
	public HttpSocketImpl(TCPChannel channel, HttpParser parser) {
		this.channel = channel;
		this.parser = parser;
		memento = parser.prepareToParse();
		state = parser.prepareToMarshal();
	}

	@Override
	public CompletableFuture<Void> connect(InetSocketAddress addr) {
		if(isRecording ) {
			dataListener = new RecordingDataListener("httpSock-", dataListener);
		}
		
		return channel.connect(addr, dataListener).thenApply(channel -> connected());
	}

	@Override
	public CompletableFuture<HttpFullResponse> send(HttpFullRequest request) {
		CompletableFuture<HttpFullResponse> future = new CompletableFuture<HttpFullResponse>();
		HttpResponseListener l = new CompletableListener(future);
		HttpData data = request.getData();
		send(request.getRequest(), l).thenCompose(w -> {
			return w.send(data);
		});
		return future;
	}
	
	private Void connected() {
		connected = true;
		return null;
	}

	@Override
	public CompletableFuture<HttpDataWriter> send(HttpRequest request, HttpResponseListener listener) {
		if(!connected)
			throw new IllegalStateException("The socket is not yet connected");

		return actuallySendRequest(request, listener);
	}

	private CompletableFuture<HttpDataWriter> actuallySendRequest(HttpRequest request, HttpResponseListener listener) {
		HttpResponseListener l = new CatchResponseListener(listener);
		ByteBuffer wrap = parser.marshalToByteBuffer(state, request);
		
		//put this on the queue before the write to be completed from the listener below
		responsesToComplete.offer(l);
		
		return channel.write(wrap).thenApply(v -> new HttpChunkWriterImpl(channel, parser, state));
	}
	
	@Override
	public CompletableFuture<Void> close() {
		if(isClosed) {
			return CompletableFuture.completedFuture(null);
		}
		
		CompletableFuture<Void> future = channel.close();
		return future.thenApply(chan -> {
			isClosed = true;
			return null;
		});
	}
	
	private class MyDataListener implements DataListener {

		private ByteAckTracker tracker = new ByteAckTracker();
		private CompletableFuture<DataWriter> future;

		@Override
		public CompletableFuture<Void> incomingData(Channel channel, ByteBuffer b) {
			DataWrapper wrapper = wrapperGen.wrapByteBuffer(b);

			int bytesIn = b.remaining();
			memento = parser.parse(memento, wrapper);
			
			List<HttpPayload> parsedMessages = memento.getParsedMessages();

			AckAggregator ack = tracker.createTracker(bytesIn, parsedMessages.size(), memento.getNumBytesJustParsed());

			for(HttpPayload msg : parsedMessages) {
				if(msg instanceof HttpData) {
					HttpData data = (HttpData) msg;
					if(data.isEndOfData())
						responsesToComplete.poll();
					
					future.thenCompose(w -> {
						return w.incomingData(data).handle((v, t) -> ack.ack(v, t));
					});
					
				} else if(msg instanceof HttpResponse) {
					future = processResponse((HttpResponse)msg)
								.handle((w, t) -> ack.ack(w, t));
				} else
					throw new IllegalStateException("invalid payload received="+msg);
			}
			
			return ack.getAckBytePayloadFuture();
		}

		private CompletableFuture<DataWriter> processResponse(HttpResponse msg) {
			if(msg.isHasChunkedTransferHeader() || msg.isHasNonZeroContentLength()) {					
				HttpResponse resp = (HttpResponse) msg;
				HttpResponseListener listener = responsesToComplete.peek();
				return listener.incomingResponse(resp, false);
			} else {
				HttpResponse resp = (HttpResponse) msg;
				HttpResponseListener listener = responsesToComplete.poll();
				return listener.incomingResponse(resp, true);
			}
		}

		@Override
		public void farEndClosed(Channel channel) {
			log.info("far end closed");
			isClosed = true;
		}

		@Override
		public void failure(Channel channel, ByteBuffer data, Exception e) {
			log.error("Failure on channel="+channel, e);
			while(!responsesToComplete.isEmpty()) {
				HttpResponseListener listener = responsesToComplete.poll();
				if(listener != null) {
					listener.failure(e);
				}
			}			
		}

	}

	@Override
	public String toString() {
		return "HttpSocketImpl [channel=" + channel + "]";
	}
	
}
