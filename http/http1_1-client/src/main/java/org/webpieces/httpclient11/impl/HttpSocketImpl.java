package org.webpieces.httpclient11.impl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpclient11.api.HttpDataWriter;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpResponseListener;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpclient11.api.HttpStreamRef;
import org.webpieces.httpclient11.api.SocketClosedException;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.MarshalState;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.handlers.RecordingDataListener;

public class HttpSocketImpl implements HttpSocket {

	private static final Logger log = LoggerFactory.getLogger(HttpSocketImpl.class);
	private static DataWrapperGenerator wrapperGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	private ChannelProxy channel;

	private boolean isClosed;
	private boolean connected;
	
	private HttpParser parser;
	private Memento memento;
	private ConcurrentLinkedQueue<HttpResponseListener> responsesToComplete = new ConcurrentLinkedQueue<>();
	private DataListener dataListener = new MyDataListener();
	private boolean isRecording = false;
	private MarshalState state;
	private boolean isConnect;
	
	public HttpSocketImpl(ChannelProxy channel, HttpParser parser) {
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
		Integer contentLength = request.getRequest().getContentLength();
		if(request.getData() == null || request.getData().getReadableSize() == 0) {
			if(contentLength != null && contentLength != 0)
				throw new IllegalArgumentException("HttpRequest has 0 Content-Length but readable size="+request.getData().getReadableSize());
		} else if(!request.getRequest().isHasNonZeroContentLength())
			throw new IllegalArgumentException("HttpRequest must have Content-Length header");
		else if(request.getRequest().getContentLength() != request.getData().getReadableSize())
			throw new IllegalArgumentException("HttpRequest Content-Length header value="
					+request.getRequest().getContentLength()+" does not match payload size="+request.getData().getReadableSize());

		CompletableFuture<HttpFullResponse> future = new CompletableFuture<HttpFullResponse>();
		HttpResponseListener l = new CompletableListener(future);
		
		HttpStreamRef streamRef = send(request.getRequest(), l);

		if(request.getData() != null && request.getData().getReadableSize() > 0) {
			HttpData data = new HttpData(request.getData(), true);
			streamRef.getWriter().thenCompose(w -> {
				return w.send(data);
			});
		}
		
		future.exceptionally( t -> {
			//we can only cancel if it is NOT keepalive or else we have to keep socket open
			if(t instanceof CancellationException && !isKeepAliveRequest(request.getRequest())) {
				streamRef.cancel("CompletableFuture cancelled by client, so cancel request");
			}
			return null;
		});
		return future;
	}
	
	private Void connected() {
		connected = true;
		return null;
	}

	@Override
	public HttpStreamRef send(HttpRequest request, HttpResponseListener listener) {
		if(!connected)
			throw new IllegalStateException("The socket is not yet connected");

		return actuallySendRequest(request, listener);
	}

	private HttpStreamRef actuallySendRequest(HttpRequest request, HttpResponseListener listener) {
		HttpResponseListener l = new CatchResponseListener(listener);
		ByteBuffer wrap = parser.marshalToByteBuffer(state, request);
		
		isConnect = false;
		if(request.getRequestLine().getMethod().getKnownStatus() == KnownHttpMethod.CONNECT)
			isConnect = true;
		
		//put this on the queue before the write to be completed from the listener below
		responsesToComplete.offer(l);
		
		boolean canSendChunks = false;
		Header header = request.getHeaderLookupStruct().getHeader(KnownHeaderName.TRANSFER_ENCODING);
		if(header != null && "chunked".equals(header.getValue()))
			canSendChunks = true;
		
		boolean canSendTheChunks = canSendChunks;
		
		CompletableFuture<HttpDataWriter> writer = channel.write(wrap).thenApply(v -> new HttpChunkWriterImpl(channel, parser, state, isConnect, canSendTheChunks));
		return new MyStreamRefImpl(writer, request);
		
	}
	
	private class MyStreamRefImpl implements HttpStreamRef {

		private CompletableFuture<HttpDataWriter> writer;
		private HttpRequest request;

		public MyStreamRefImpl(CompletableFuture<HttpDataWriter> writer, HttpRequest request) {
			this.writer = writer;
			this.request = request;
		}

		@Override
		public CompletableFuture<HttpDataWriter> getWriter() {
			return writer;
		}

		@Override
		public CompletableFuture<Void> cancel(Object reason) {
			if(!isKeepAliveRequest(request)) {
				return close();
			}
			
			//nothing we can do in http1.1 since keep alive was sent and cancel means to cancel the 
			//previous request.  TODO(dhiller): we should probably start disccarding the response coming back but alas
			//clients can do that too (and http1.1 is going away)
			return CompletableFuture.completedFuture(null);
		}
		
	}
	
	private boolean isKeepAliveRequest(HttpRequest req) {
		Header header = req.getHeaderLookupStruct().getHeader(KnownHeaderName.CONNECTION);
		if(header != null && "keep-alive".equals(header.getValue())) {
			return true;
		}
		return false;
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
		private CompletableFuture<HttpDataWriter> dataWriterFuture;
		private boolean connectResponseReceived;

		@Override
		public CompletableFuture<Void> incomingData(Channel channel, ByteBuffer b) {
			DataWrapper wrapper = wrapperGen.wrapByteBuffer(b);

			if(connectResponseReceived) {
				//special case of just sending data through as we are doing proxying SSL stuff
				HttpData data = new HttpData(wrapper, false);
				return dataWriterFuture.thenCompose(w -> {
					return w.send(data);
				});
			}
			
			memento = parser.parse(memento, wrapper);
			
			if(memento.getParsedMessages().size() == 1 && isConnect) {
				//If it was a connect, we should get 1 response AND from then on, we need to switch to 
				//pure data since this is an SSL tunnel through the proxy
				connectResponseReceived = true;
			}
			
			if(memento.getNumBytesJustParsed() == 0)
				return CompletableFuture.completedFuture(null); //ack the future as we need more data.  there is nothing to track here

			List<HttpPayload> parsedMessages = memento.getParsedMessages();

			CompletableFuture<Void> allFutures = CompletableFuture.completedFuture(null);
			for(HttpPayload msg : parsedMessages) {
				if(msg instanceof HttpData) {
					HttpData data = (HttpData) msg;
					if(data.isEndOfData())
						responsesToComplete.poll();

					//w.send needs to be sent IN SEQUENCE by thenCompose with previous w.send
					allFutures = allFutures
							.thenCompose( voidd -> dataWriterFuture)
							.thenCompose(w -> w.send(data));

				} else if(msg instanceof HttpResponse) {
					dataWriterFuture = processResponse((HttpResponse)msg);

					//Need to chain all futures into allFutures
					allFutures = allFutures.thenCompose(s -> dataWriterFuture).thenApply(s -> (Void)null);

				} else
					throw new IllegalStateException("invalid payload received="+msg);
			}
			
			return allFutures;
		}

		private CompletableFuture<HttpDataWriter> processResponse(HttpResponse msg) {
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
			log.info("far end closed. socket("+channel+")");
			isClosed = true;
			while(!responsesToComplete.isEmpty()) {
				HttpResponseListener listener = responsesToComplete.poll();
				if(listener != null) {
					listener.socketClosed();
				}
			}
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

	@Override
	public boolean isClosed() {
		return isClosed;
	}
	
}
