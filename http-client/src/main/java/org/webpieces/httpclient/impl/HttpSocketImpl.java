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
import org.webpieces.httpclient.api.HttpSocket;
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
import com.webpieces.httpparser.api.dto.HttpMessage;
import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpResponse;

public class HttpSocketImpl implements HttpSocket, Closeable {

	private static final Logger log = LoggerFactory.getLogger(HttpSocketImpl.class);
	private TCPChannel channel;
	private HttpParser parser;
	private DataWrapperGenerator wrapperGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private boolean isClosed;
	private Memento memento;
	private ConcurrentLinkedQueue<CompletableFuture<HttpResponse>> futuresToComplete = new ConcurrentLinkedQueue<>();
	private MyDataListener dataListener = new MyDataListener();
	
	public HttpSocketImpl(ChannelManager mgr, String idForLogging, HttpParser parser) {
		channel = mgr.createTCPChannel(idForLogging, dataListener);
		this.parser = parser;
		memento = parser.prepareToParse();
	}

	@Override
	public CompletableFuture<HttpSocket> connect(SocketAddress addr) {
		return channel.connect(addr).thenApply(channel -> this);
	}
	
	@Override
	public CompletableFuture<HttpResponse> send(HttpRequest request) {
		byte[] bytes = parser.marshalToBytes(request);
		ByteBuffer wrap = ByteBuffer.wrap(bytes);
		
		//put this on the queue before the write to be completed from the listener below
		CompletableFuture<HttpResponse> future = new CompletableFuture<HttpResponse>();
		futuresToComplete.offer(future);
		
		CompletableFuture<Channel> write = channel.write(wrap);
		return write.thenCompose(p -> future);
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
		
		//do we need an isClosing state and cache that future?  (I don't think so but time will tell)
		
		CompletableFuture<Channel> future = channel.close();
		return future.thenApply(chan -> {
			isClosed = true;
			return this;
		});
	}
	
	private class MyDataListener implements DataListener {

		@Override
		public void incomingData(Channel channel, ByteBuffer b) {
			DataWrapper wrapper = wrapperGen.wrapByteBuffer(b);
			memento = parser.parse(memento, wrapper);

			List<HttpMessage> parsedMessages = memento.getParsedMessages();
			for(HttpMessage msg : parsedMessages) {
				HttpResponse resp = (HttpResponse) msg;
				CompletableFuture<HttpResponse> future = futuresToComplete.remove();
				future.complete(resp);
			}
		}

		@Override
		public void farEndClosed(Channel channel) {
			while(!futuresToComplete.isEmpty()) {
				CompletableFuture<HttpResponse> future = futuresToComplete.poll();
				if(future != null) {
					future.completeExceptionally(new NioClosedChannelException("Remote end closed before responses were received"));
				}
			}
			
			isClosed = true;
		}

		@Override
		public void failure(Channel channel, ByteBuffer data, Exception e) {
			log.warn("Failure on channel="+channel, e);
		}

		@Override
		public void applyBackPressure(Channel channel) {
		}

		@Override
		public void releaseBackPressure(Channel channel) {
		}
	}
	
}
