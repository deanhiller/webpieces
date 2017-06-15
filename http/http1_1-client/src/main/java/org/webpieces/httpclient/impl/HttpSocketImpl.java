package org.webpieces.httpclient.impl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpclient.api.DataWriter;
import org.webpieces.httpclient.api.HttpDataWriter;
import org.webpieces.httpclient.api.HttpFullRequest;
import org.webpieces.httpclient.api.HttpFullResponse;
import org.webpieces.httpclient.api.HttpResponseListener;
import org.webpieces.httpclient.api.HttpSocket;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.MarshalState;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.exceptions.NioClosedChannelException;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.handlers.RecordingDataListener;

import com.webpieces.util.locking.PermitQueue;

public class HttpSocketImpl implements HttpSocket {

	private static final Logger log = LoggerFactory.getLogger(HttpSocketImpl.class);
	private static DataWrapperGenerator wrapperGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	private TCPChannel channel;

	private CompletableFuture<Void> connectFuture;
	private boolean isClosed;
	private boolean connected;
	private ConcurrentLinkedQueue<PendingRequest> pendingRequests = new ConcurrentLinkedQueue<>();
	
	private HttpParser parser;
	private Memento memento;
	private ConcurrentLinkedQueue<HttpResponseListener> responsesToComplete = new ConcurrentLinkedQueue<>();
	private DataListener dataListener = new MyDataListener();
	private boolean isRecording = false;
	private MarshalState state;
	
	private PermitQueue queue;
	
	public HttpSocketImpl(TCPChannel channel, HttpParser parser) {
		this.channel = channel;
		this.parser = parser;
		memento = parser.prepareToParse();
		state = parser.prepareToMarshal();
		queue = new PermitQueue("channel", 1, 3000, 100);
	}

	@Override
	public CompletableFuture<Void> connect(InetSocketAddress addr) {
		if(isRecording ) {
			dataListener = new RecordingDataListener("httpSock-", dataListener);
		}
		
		connectFuture = channel.connect(addr, dataListener).thenApply(channel -> connected());
		return connectFuture;
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
	
	private synchronized Void connected() {
		connected = true;
		
		while(!pendingRequests.isEmpty()) {
			PendingRequest req = pendingRequests.remove();
			actuallySendRequest(req.getFuture(), req.getRequest(), req.getListener());
		}
		
		return null;
	}

	@Override
	public CompletableFuture<HttpDataWriter> send(HttpRequest request, HttpResponseListener listener) {
		if(connectFuture == null)
			throw new IllegalArgumentException("You must at least call httpSocket.connect first(it "
					+ "doesn't have to complete...you just have to call it before caling send)");

		CompletableFuture<HttpDataWriter> future = new CompletableFuture<>();
		boolean wasConnected = false;
		synchronized (this) {
			if(!connected) {
				pendingRequests.add(new PendingRequest(future, request, listener));
			} else
				wasConnected = true;
		}
		
		if(wasConnected) 
			actuallySendRequest(future, request, listener);
		
		return future;
	}

	private void actuallySendRequest(CompletableFuture<HttpDataWriter> future, HttpRequest request, HttpResponseListener listener) {
		HttpResponseListener l = new CatchResponseListener(listener);
		ByteBuffer wrap = parser.marshalToByteBuffer(state, request);
		
		//put this on the queue before the write to be completed from the listener below
		responsesToComplete.offer(l);
		
		log.info("sending request now. req="+request.getRequestLine().getUri());
		CompletableFuture<Void> write = channel.write(wrap);
		
		
		write.handle((c, t) -> chainToFuture(c, t, future));
	}
	
	private Void chainToFuture(Void v, Throwable t, CompletableFuture<HttpDataWriter> future) {
		if(t != null) {
			future.completeExceptionally(new RuntimeException(t));
			return null;
		}
		
		HttpChunkWriterImpl impl = new HttpChunkWriterImpl(channel, parser, state);
		future.complete(impl);
		
		return null;
	}
	
	@Override
	public CompletableFuture<Void> close() {
		if(isClosed) {
			return CompletableFuture.completedFuture(null);
		}
		
		cleanUpPendings("You closed the socket");
		
		CompletableFuture<Void> future = channel.close();
		return future.thenApply(chan -> {
			isClosed = true;
			return null;
		});
	}

	private void cleanUpPendings(String msg) {
		//do we need an isClosing state and cache that future?  (I don't think so but time will tell)
		while(!responsesToComplete.isEmpty()) {
			HttpResponseListener listener = responsesToComplete.poll();
			if(listener != null) {
				listener.failure(new NioClosedChannelException(msg+" before responses were received"));
			}
		}
		
		synchronized (this) {
			while(!pendingRequests.isEmpty()) {
				PendingRequest pending = pendingRequests.poll();
				pending.getListener().failure(new NioClosedChannelException(msg+" before requests were sent"));
			}
		}
	}
	
	private class MyDataListener implements DataListener {

		private CompletableFuture<DataWriter> future;

		@Override
		public CompletableFuture<Void> incomingData(Channel channel, ByteBuffer b) {
			log.info("size="+b.remaining());
			DataWrapper wrapper = wrapperGen.wrapByteBuffer(b);
			memento = parser.parse(memento, wrapper);

			List<HttpPayload> parsedMessages = memento.getParsedMessages();
			for(HttpPayload msg : parsedMessages) {
				if(msg instanceof HttpData) {
					HttpData data = (HttpData) msg;
					if(data.isEndOfData())
						responsesToComplete.poll();
					
					future = future.thenCompose(w -> {
						return w.incomingData(data);
					});
					
				} else if(msg instanceof HttpResponse) {
					future = processResponse((HttpResponse)msg);
				} else
					throw new IllegalStateException("invalid payload received="+msg);
			}
			return future.thenApply(w -> null);
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
			cleanUpPendings("Remote end closed");		
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
