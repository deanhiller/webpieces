package org.webpieces.httpclient.impl;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpclient.api.CloseListener;
import org.webpieces.httpclient.api.HttpSocket;
import org.webpieces.httpclient.api.ResponseListener;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.exceptions.NioClosedChannelException;
import org.webpieces.nio.api.handlers.DataListener;

import com.webpieces.data.api.DataWrapper;
import com.webpieces.data.api.DataWrapperGenerator;
import com.webpieces.data.api.DataWrapperGeneratorFactory;
import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.Memento;
import com.webpieces.httpparser.api.dto.HttpChunk;
import com.webpieces.httpparser.api.dto.HttpPayload;
import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpResponse;

public class HttpSocketImpl implements HttpSocket, Closeable {

	private static final Logger log = LoggerFactory.getLogger(HttpSocketImpl.class);
	private static DataWrapperGenerator wrapperGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	private TCPChannel channel;

	private CompletableFuture<HttpSocket> connectFuture;
	private boolean isClosed;
	private boolean connected;
	private ConcurrentLinkedQueue<PendingRequest> pendingRequests = new ConcurrentLinkedQueue<>();
	
	private HttpParser parser;
	private Memento memento;
	private ConcurrentLinkedQueue<ResponseListener> responsesToComplete = new ConcurrentLinkedQueue<>();
	private MyDataListener dataListener = new MyDataListener();
	private CloseListener closeListener;
	
	public HttpSocketImpl(ChannelManager mgr, String idForLogging, HttpParser parser, CloseListener listener) {
		channel = mgr.createTCPChannel(idForLogging);
		this.parser = parser;
		memento = parser.prepareToParse();
		this.closeListener = listener;
	}

	@Override
	public CompletableFuture<HttpSocket> connect(SocketAddress addr) {
		connectFuture = channel.connect(addr, dataListener).thenApply(channel -> connected());
		return connectFuture;
	}

	@Override
	public CompletableFuture<HttpResponse> send(HttpRequest request) {
		CompletableFuture<HttpResponse> future = new CompletableFuture<HttpResponse>();
		ResponseListener l = new CompletableListener(future);
		send(request, l);
		return future;
	}
	
	private synchronized HttpSocket connected() {
		connected = true;
		
		while(!pendingRequests.isEmpty()) {
			PendingRequest req = pendingRequests.remove();
			actuallySendRequest(req.getRequest(), req.getListener());
		}
		
		return this;
	}

	@Override
	public void send(HttpRequest request, ResponseListener listener) {
		if(connectFuture == null) 
			throw new IllegalArgumentException("You must at least call httpSocket.connect first(it "
					+ "doesn't have to complete...you just have to call it before caling send)");

		boolean wasConnected = false;
		synchronized (this) {
			if(!connected) {
				pendingRequests.add(new PendingRequest(request, listener));
			} else
				wasConnected = true;
		}
		
		if(wasConnected) 
			actuallySendRequest(request, listener);
	}

	private void actuallySendRequest(HttpRequest request, ResponseListener listener) {
		ResponseListener l = new CatchResponseListener(listener);
		byte[] bytes = parser.marshalToBytes(request);
		ByteBuffer wrap = ByteBuffer.wrap(bytes);
		
		//put this on the queue before the write to be completed from the listener below
		responsesToComplete.offer(l);
		
		log.info("sending request now. req="+request.getRequestLine().getUri());
		CompletableFuture<Channel> write = channel.write(wrap);
		write.exceptionally(e -> fail(l, e));
	}
	
	private Channel fail(ResponseListener l, Throwable e) {
		l.failure(e);
		return null;
	}

	@Override
	public void close() throws IOException {
		if(isClosed)
			return;
		
		//best effort and ignore exception except log it
		CompletableFuture<HttpSocket> future = closeSocket();
		future.exceptionally(e -> {
			log.info("close failed", e);
			return this;
		});
	}
	
	@Override
	public CompletableFuture<HttpSocket> closeSocket() {
		if(isClosed) {
			return CompletableFuture.completedFuture(this);
		}
		
		cleanUpPendings("You closed the socket");
		
		CompletableFuture<Channel> future = channel.close();
		return future.thenApply(chan -> {
			isClosed = true;
			return this;
		});
	}

	private void cleanUpPendings(String msg) {
		//do we need an isClosing state and cache that future?  (I don't think so but time will tell)
		while(!responsesToComplete.isEmpty()) {
			ResponseListener listener = responsesToComplete.poll();
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
		private boolean processingChunked = false;

		@Override
		public void incomingData(Channel channel, ByteBuffer b) {
			DataWrapper wrapper = wrapperGen.wrapByteBuffer(b);
			memento = parser.parse(memento, wrapper);

			List<HttpPayload> parsedMessages = memento.getParsedMessages();
			for(HttpPayload msg : parsedMessages) {
				if(processingChunked) {
					HttpChunk chunk = (HttpChunk) msg;
					ResponseListener listener = responsesToComplete.peek();
					if(chunk.isLastChunk()) {
						responsesToComplete.poll();
					}
					
					listener.incomingChunk(chunk, chunk.isLastChunk());
				} else if(!msg.isHasChunkedTransferHeader()) {
					HttpResponse resp = (HttpResponse) msg;
					ResponseListener listener = responsesToComplete.poll();
					listener.incomingResponse(resp, true);
				} else {
					processingChunked = true;
					HttpResponse resp = (HttpResponse) msg;
					ResponseListener listener = responsesToComplete.peek();
					listener.incomingResponse(resp, false);
				}
			}
		}

		@Override
		public void farEndClosed(Channel channel) {
			isClosed = true;
			log.info("far end closed. channel="+channel, new RuntimeException());
			cleanUpPendings("Remote end closed");
			
			if(closeListener != null)
				closeListener.farEndClosed(HttpSocketImpl.this);
		
		}

		@Override
		public void failure(Channel channel, ByteBuffer data, Exception e) {
			log.warn("Failure on channel="+channel, e);
			while(!responsesToComplete.isEmpty()) {
				ResponseListener listener = responsesToComplete.poll();
				if(listener != null) {
					listener.failure(e);
				}
			}			
		}

		@Override
		public void applyBackPressure(Channel channel) {
			
		}

		@Override
		public void releaseBackPressure(Channel channel) {
		}
	}
	
}
