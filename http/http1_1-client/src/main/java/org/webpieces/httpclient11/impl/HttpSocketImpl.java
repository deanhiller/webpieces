package org.webpieces.httpclient11.impl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CancellationException;

import org.webpieces.nio.api.channels.HostWithPort;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpclient11.api.HttpDataWriter;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpResponseListener;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpclient11.api.HttpSocketListener;
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
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.handlers.RecordingDataListener;

public class HttpSocketImpl implements HttpSocket {

	private static final Logger log = LoggerFactory.getLogger(HttpSocketImpl.class);
	private static DataWrapperGenerator wrapperGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private final String svrSocket;

	private ChannelProxy channel;

	private boolean isClosed;
	private boolean connected;
	
	private HttpParser parser;
	private Memento memento;
	private ConcurrentLinkedQueue<CatchResponseListener> responsesToComplete = new ConcurrentLinkedQueue<>();
	private DataListener dataListener = new MyDataListener();
	private boolean isRecording = false;
	private MarshalState state;
	private boolean isConnect;
	private HttpSocketListener socketListener;
	private boolean isHttps;
	
	public HttpSocketImpl(ChannelProxy channel, HttpParser parser, HttpSocketListener socketListener, boolean isHttps) {
		this.isHttps = isHttps;
		if(socketListener == null || channel == null || parser == null)
			throw new IllegalArgumentException("no args can be null");

		this.svrSocket = MDC.get("svrSocket");

		this.channel = channel;
		this.parser = parser;
		this.socketListener = new ProxyClose(socketListener, svrSocket);
		memento = parser.prepareToParse();
		state = parser.prepareToMarshal();
	}

	@Override
	public XFuture<Void> connect(HostWithPort addr) {
		if(isRecording ) {
			dataListener = new RecordingDataListener("httpSock-", dataListener);
		}

		return channel.connect(addr, dataListener).thenApply(channel -> connected());
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	@Override
	public XFuture<Void> connect(InetSocketAddress addr) {
		if(isRecording ) {
			dataListener = new RecordingDataListener("httpSock-", dataListener);
		}
		
		return channel.connect(addr, dataListener).thenApply(channel -> connected());
	}

	@Override
	public XFuture<HttpFullResponse> send(HttpFullRequest request) {
		Integer contentLength = request.getRequest().getContentLength();
		if(request.getData() == null || request.getData().getReadableSize() == 0) {
			if(contentLength != null && contentLength != 0)
				throw new IllegalArgumentException("HttpRequest has 0 Content-Length but readable size="+request.getData().getReadableSize());
		} else if(!request.getRequest().isHasNonZeroContentLength())
			throw new IllegalArgumentException("HttpRequest must have Content-Length header");
		else if(request.getRequest().getContentLength() != request.getData().getReadableSize())
			throw new IllegalArgumentException("HttpRequest Content-Length header value="
					+request.getRequest().getContentLength()+" does not match payload size="+request.getData().getReadableSize());

		XFuture<HttpFullResponse> future = new XFuture<HttpFullResponse>();
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
				streamRef.cancel("XFuture cancelled by client, so cancel request");
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
		CatchResponseListener l = new CatchResponseListener(listener, svrSocket);
		ByteBuffer wrap = parser.marshalToByteBuffer(state, request);
		
		isConnect = false;
		if(request.getRequestLine().getMethod().getKnownStatus() == KnownHttpMethod.CONNECT)
			isConnect = true;
		
		//put this on the queue before the write to be completed from the listener below
		responsesToComplete.offer(l);

		int bytesTracker = 0;
		if(request.isHasChunkedTransferHeader()) {
			bytesTracker = -1;
		} else if(request.getContentLength() != null && request.getContentLength() > 0) {
			bytesTracker = request.getContentLength();
		}

		int maxBytesToSend = bytesTracker;
		XFuture<HttpDataWriter> writer = channel.write(wrap).thenApply(v -> new HttpChunkWriterImpl(channel, parser, state, isConnect, maxBytesToSend));
		return new MyStreamRefImpl(writer, request);
		
	}
	
	private class MyStreamRefImpl implements HttpStreamRef {

		private XFuture<HttpDataWriter> writer;
		private HttpRequest request;

		public MyStreamRefImpl(XFuture<HttpDataWriter> writer, HttpRequest request) {
			this.writer = writer;
			this.request = request;
		}

		@Override
		public XFuture<HttpDataWriter> getWriter() {
			return writer;
		}

		@Override
		public XFuture<Void> cancel(Object reason) {
			if(!isKeepAliveRequest(request)) {
				return close();
			}
			
			//nothing we can do in http1.1 since keep alive was sent and cancel means to cancel the 
			//previous request.  TODO(dhiller): we should probably start disccarding the response coming back but alas
			//clients can do that too (and http1.1 is going away)
			return XFuture.completedFuture(null);
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
	public XFuture<Void> close() {
		if(isClosed) {
			return XFuture.completedFuture(null);
		}
		
		XFuture<Void> future = channel.close();
		return future.thenApply(chan -> {
			isClosed = true;
			return null;
		});
	}

	private class MyDataListener implements DataListener {
		private static final String FUTURE_PROCESS_KEY = "__webpiecesFutureProcessKey";
		private XFuture<HttpDataWriter> dataWriterFuture;
		private boolean connectResponseReceived;

		@Override
		public XFuture<Void> incomingData(Channel channel, ByteBuffer b) {
			DataWrapper wrapper = wrapperGen.wrapByteBuffer(b);

			DataWrapper leftOverData = null;
			if(connectResponseReceived) {
				return sendDataAfterHttpConnect(wrapper);
			} else if(isConnect) {
				//If it was a connect, we should get 1 response AND from then on, we need to switch to 
				//pure data since this is an SSL tunnel through the proxy
				connectResponseReceived = true;
				memento = parser.parseOnlyHeaders(memento, wrapper);
				leftOverData = memento.getLeftOverData();
			} else {
				memento = parser.parse(memento, wrapper);
			}
			
			if(memento.getNumBytesJustParsed() == 0)
				return XFuture.completedFuture(null); //ack the future as we need more data.  there is nothing to track here

			List<HttpPayload> parsedMessages = memento.getParsedMessages();

			ChannelSession session = channel.getSession();
			ResponseSession rs = (ResponseSession) session.get(FUTURE_PROCESS_KEY);
			if(rs == null) {
				rs = new ResponseSession();
				session.put(FUTURE_PROCESS_KEY, rs);
			}
				
			XFuture<Void> future = rs.getProcessFuture();
			
			for(HttpPayload msg : parsedMessages) {
				if(msg instanceof HttpData) {
					HttpData data = (HttpData) msg;
					if(data.isEndOfData())
						responsesToComplete.poll();

					//w.send needs to be sent IN SEQUENCE by thenCompose with previous w.send
					future = future
							.thenCompose( voidd -> dataWriterFuture)
							.thenCompose(w -> w.send(data));

				} else if(msg instanceof HttpResponse) {

					//Need to make ALL sends serialized one after the other including previous 
					// processResponse calls
					future = future
								.thenCompose(s -> processResponse((HttpResponse)msg))
								.thenApply(s -> (Void)null);

				} else
					throw new IllegalStateException("invalid payload received="+msg);
			}
			
			rs.setProcessFuture(future);
			
			if(connectResponseReceived && leftOverData.getReadableSize() > 0) {
				return sendDataAfterHttpConnect(leftOverData);
			}
			
			return future;
		}

		private XFuture<Void> sendDataAfterHttpConnect(DataWrapper wrapper) {
			//special case of just sending data through as we are doing proxying SSL stuff
			HttpData data = new HttpData(wrapper, false);
			return dataWriterFuture.thenCompose(w -> {
				return w.send(data);
			});
		}

		private XFuture<HttpDataWriter> processResponse(HttpResponse msg) {
			boolean isComplete;
			CatchResponseListener listener;
			if(msg.isHasChunkedTransferHeader() || msg.isHasNonZeroContentLength()) {					
				listener = responsesToComplete.peek();
				isComplete = false;
			} else {
				isComplete = true;
				listener = responsesToComplete.poll();
			}

			HttpResponse resp = (HttpResponse) msg;
			dataWriterFuture = listener.incomingResponse(resp, isComplete);
			return dataWriterFuture;
		}

		@Override
		public void farEndClosed(Channel channel) {
			isClosed = true;
			socketListener.socketClosed(HttpSocketImpl.this);
			
			while(!responsesToComplete.isEmpty()) {
				CatchResponseListener listener = responsesToComplete.poll();
				listener.failure(new SocketClosedException("Socket was closed by remote end"));
			}
		}

		@Override
		public void failure(Channel channel, ByteBuffer data, Exception e) {
			log.error("Failure on channel="+channel, e);
			while(!responsesToComplete.isEmpty()) {
				CatchResponseListener listener = responsesToComplete.poll();
				if(listener != null) {
					listener.failure(e);
				}
			}			
		}

	}

	@Override
	public String toString() {
		return "HttpSocketImpl [channel=" + channel.getId() + "isHttps="+isHttps+" tcpSecure="+channel.isSecure()+"]";
	}

	@Override
	public boolean isClosed() {
		return isClosed;
	}
	
}
