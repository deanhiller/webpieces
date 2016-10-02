package org.webpieces.httpclient.impl;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.net.ssl.SSLEngine;
import javax.xml.bind.DatatypeConverter;
import javax.xml.crypto.Data;

import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.ParserResult;
import com.webpieces.http2parser.api.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpclient.api.CloseListener;
import org.webpieces.httpclient.api.HttpSocket;
import org.webpieces.httpclient.api.HttpsSslEngineFactory;
import org.webpieces.httpclient.api.ResponseListener;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.dto.*;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.exceptions.NioClosedChannelException;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.handlers.RecordingDataListener;

import static com.webpieces.http2parser.api.dto.Http2FrameType.SETTINGS;
import static org.webpieces.httpclient.impl.HttpSocketImpl.Protocol.HTTP11;
import static org.webpieces.httpclient.impl.HttpSocketImpl.Protocol.HTTP2;
import static org.webpieces.httpclient.impl.Stream.StreamStatus.*;

public class HttpSocketImpl implements HttpSocket, Closeable {

	private static final Logger log = LoggerFactory.getLogger(HttpSocketImpl.class);
	private static DataWrapperGenerator wrapperGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	private TCPChannel channel;

	private CompletableFuture<HttpSocket> connectFuture;
	private boolean isClosed;
	private boolean connected;

	private enum Protocol { HTTP11, HTTP2 };
	private Protocol protocol = HTTP11;

	// HTTP 2
	private Http2Parser http2Parser;
	private boolean tryHttp2 = true;
	private Http2Settings preferredSettings = new Http2Settings();
	private Http2Settings serverSettings = new Http2Settings();
	private Http2Settings clientSettings = new Http2Settings();
	private ConcurrentHashMap<Integer, Stream> activeStreams = new ConcurrentHashMap<>();

	// HTTP 1.1
	private HttpParser http11Parser;
	private ConcurrentLinkedQueue<PendingRequest> pendingRequests = new ConcurrentLinkedQueue<>();
	private ConcurrentLinkedQueue<ResponseListener> responsesToComplete = new ConcurrentLinkedQueue<>();

	private ProxyDataListener dataListener = new ProxyDataListener();
	private CloseListener closeListener;
	private HttpsSslEngineFactory factory;
	private ChannelManager mgr;
	private String idForLogging;
	private boolean isRecording = false;

	public HttpSocketImpl(ChannelManager mgr, String idForLogging, HttpsSslEngineFactory factory, HttpParser parser2,
			CloseListener listener) {
		this.factory = factory;
		this.mgr = mgr;
		this.idForLogging = idForLogging;
		this.http11Parser = parser2;
		this.closeListener = listener;
	}

	@Override
	public CompletableFuture<HttpSocket> connect(InetSocketAddress addr) {
		if(factory == null) {
			channel = mgr.createTCPChannel(idForLogging);
		} else {
			SSLEngine engine = factory.createSslEngine(addr.getHostName(), addr.getPort());
			channel = mgr.createTCPChannel(idForLogging, engine);
		}
		DataListener dataListenerToUse;

		if(isRecording) {
			dataListenerToUse = new RecordingDataListener("httpSock-", dataListener);
		} else {
			dataListenerToUse = dataListener;
		}
		
		connectFuture = channel.connect(addr, dataListenerToUse).thenCompose(channel -> {
			if(tryHttp2) {
				return negotiateHttpVersion();
			}
			else {
				return CompletableFuture.completedFuture(connected());
			}
		});
		return connectFuture;
	}

	private void sendHttp2Preface() {
		String prefaceString = "505249202a20485454502f322e300d0a0d0a534d0d0a0d0a";
		channel.write(ByteBuffer.wrap(DatatypeConverter.parseHexBinary(prefaceString)));
		channel.write(ByteBuffer.wrap(http2Parser.marshal(preferredSettings).createByteArray()));
	}

	private CompletableFuture<HttpSocket> negotiateHttpVersion() {
		// First check if ALPN says HTTP2, in which case, set the protocol to HTTP2 and we're done
		if (false) { // alpn says HTTP? or we have some other prior knowledge
			protocol = HTTP2;
			dataListener.setProtocol(HTTP2);
			sendHttp2Preface();

			return CompletableFuture.completedFuture(connected());

		} else { // Try the HTTP1.1 upgrade technique
			HttpRequest upgradeRequest = new HttpRequest();
			HttpRequestLine requestLine = new HttpRequestLine();
			requestLine.setMethod(KnownHttpMethod.HEAD);
			requestLine.setUri(new HttpUri("/"));
			requestLine.setVersion(new HttpVersion());
			upgradeRequest.setRequestLine(requestLine);
			upgradeRequest.addHeader(new Header("Connection", "Upgrade, HTTP2-Settings"));
			upgradeRequest.addHeader(new Header("Upgrade", "h2c"));

			upgradeRequest.addHeader(new Header("HTTP2-Settings",
					Base64.getEncoder().encodeToString(http2Parser.marshal(preferredSettings).createByteArray())));

			CompletableFuture<HttpResponse> response = sendIgnoreConnected(upgradeRequest);
			return response.thenApply(r -> {
				if(r.getStatusLine().getStatus().getCode() != 101) {
					return connected();
				} else {
					protocol = HTTP2;
					dataListener.setProtocol(HTTP2);
					sendHttp2Preface();

					return connected();
				}
			});
		}
	}

	private CompletableFuture<HttpResponse> sendIgnoreConnected(HttpRequest request) {
		CompletableFuture<HttpResponse> future = new CompletableFuture<HttpResponse>();
		ResponseListener l = new CompletableListener(future);
		actuallySendRequest(request, l);
		return future;
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
		if(protocol == HTTP11) {
			ResponseListener l = new CatchResponseListener(listener);
			byte[] bytes = http11Parser.marshalToBytes(request);
			ByteBuffer wrap = ByteBuffer.wrap(bytes);

			//put this on the queue before the write to be completed from the listener below
			responsesToComplete.offer(l);

			log.info("sending request now. req=" + request.getRequestLine().getUri());
			CompletableFuture<Channel> write = channel.write(wrap);
			write.exceptionally(e -> fail(l, e));
		} else { // HTTP2

		}
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

	private class ProxyDataListener implements DataListener {
		private Protocol protocol = HTTP11;
		private Map<Protocol, DataListener> dataListenerMap = new HashMap<>();

		void setProtocol(Protocol protocol) {
			this.protocol = protocol;
		}

		ProxyDataListener() {
			dataListenerMap.put(HTTP11, new Http11DataListener());
			dataListenerMap.put(HTTP2, new Http2DataListener());
		}

		@Override
		public void incomingData(Channel channel, ByteBuffer b) {
			dataListenerMap.get(protocol).incomingData(channel, b);
		}

		@Override
		public void farEndClosed(Channel channel) {
			dataListenerMap.get(protocol).farEndClosed(channel);
		}

		@Override
		public void failure(Channel channel, ByteBuffer data, Exception e) {
			dataListenerMap.get(protocol).failure(channel, data, e);
		}

		@Override
		public void applyBackPressure(Channel channel) {
			dataListenerMap.get(protocol).applyBackPressure(channel);
		}

		@Override
		public void releaseBackPressure(Channel channel) {
			dataListenerMap.get(protocol).releaseBackPressure(channel);
		}
	}

	private class Http2DataListener implements DataListener {
		private DataWrapper oldData = http2Parser.prepareToParse();
		private boolean gotSettings = false;

		private void handleEndStream(boolean isComplete, Stream stream) {
			stream.getListener().incomingResponse(stream.getResponse(), stream.getRequest(), isComplete);

			if(isComplete) {
				// Make sure status can accept ES
				switch(stream.getStatus()) {
					case OPEN:
					case HALF_CLOSED_LOCAL:
						stream.setStatus(HALF_CLOSED_REMOTE);

						// Now send ES back
						Http2Data sendFrame = new Http2Data();
						sendFrame.setEndStream(true);
						channel.write(ByteBuffer.wrap(http2Parser.marshal(sendFrame).createByteArray()));

						stream.setStatus(CLOSED);
						break;
					default:
						// throw error here
				}
			}
		}

		private void handleData(Http2Data frame, Stream stream) {
			// Only allowable if stream is open or half closed local
			switch(stream.getStatus()) {
				case OPEN:
				case HALF_CLOSED_LOCAL:
					stream.getResponse().appendBody(frame.getData());
					boolean isComplete = frame.isEndStream();
					handleEndStream(isComplete, stream);
					break;
				default:
					// Throw
			}
		}

		private HttpResponse createResponseFromHeaders(Map<String, String> headers) {
			HttpResponse response = new HttpResponse();

			// Set special header
			String statusString = headers.get(":status");
			// TODO: throw if no such header

			HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
			HttpResponseStatus status = new HttpResponseStatus();
			status.setCode(Integer.parseInt(statusString));
			// TODO: throw if can't parse

			statusLine.setStatus(status);
			response.setStatusLine(statusLine);

			// Set all other headers
			for(Map.Entry<String, String> entry: headers.entrySet()) {
				if(!entry.getKey().equals(":status"))
					response.addHeader(new Header(entry.getKey(), entry.getValue()));
			}

			return response;
		}

		private HttpRequest createRequestFromHeaders(Map<String, String> headers) {
			HttpRequest request = new HttpRequest();

			// Set special headers
			// TODO: throw if no such headers
			String method = headers.get(":method");
			String scheme = headers.get(":scheme");
			String authority = headers.get(":authority");
			String path = headers.get(":path");

			// See https://svn.tools.ietf.org/svn/wg/httpbis/specs/rfc7230.html#asterisk-form
			if(method.toLowerCase().equals("options") && path.equals("*")) {
				path = "";
			}

			HttpRequestLine requestLine = new HttpRequestLine();
			requestLine.setUri(new HttpUri(String.format("{}://{}{}", scheme, authority, path)));
			requestLine.setMethod(new HttpRequestMethod(method));
			request.setRequestLine(requestLine);

			List<String> specialHeaders = Arrays.asList(":method", ":scheme", ":authority", ":path");

			// Set all other headers
			for(Map.Entry<String, String> entry: headers.entrySet()) {
				if(!specialHeaders.contains(entry.getKey()))
					request.addHeader(new Header(entry.getKey(), entry.getValue()));
			}
			// hm do we need this?
			request.addHeader(new Header("Host", authority));

			return request;
		}

		private void handleHeaders(Http2Headers frame, Stream stream) {
			switch (stream.getStatus()) {
				case IDLE:
					break;
				default:
					// throw appropriate error
			}

			HttpResponse response = createResponseFromHeaders(frame.getHeaders());
			stream.setResponse(response);
			if(frame.isEndHeaders()) {
				// If we are done getting headers
				stream.setStatus(OPEN);
			} else {
				stream.setStatus(WAITING_MORE_NORMAL_HEADERS);
			}
			boolean isComplete = frame.isEndStream();
			handleEndStream(isComplete, stream);
		}


		private void handlePriority(Http2Priority frame, Stream stream) {
			// ignore priority for now. priority can be received in any state.

		}

		private void handleRstStream(Http2RstStream frame, Stream stream) {
			switch(stream.getStatus()) {
				case OPEN:
				case HALF_CLOSED_REMOTE:
				case HALF_CLOSED_LOCAL:
				case RESERVED_LOCAL:
				case RESERVED_REMOTE:
					stream.setStatus(CLOSED);
					break;
				default:
					// throw the error here
			}
		}

		private void handlePushPromise(Http2PushPromise frame, Stream stream) {
			// Can get this on any stream id, creates a new stream
			Stream promisedStream = new Stream();
			int newStreamId = frame.getPromisedStreamId();

			// TODO: make sure streamid is valid
			activeStreams.put(newStreamId, promisedStream);
			HttpRequest request = createRequestFromHeaders(frame.getHeaders());
			stream.setRequest(request);

			// Uses the same listener as the stream it came in on
			stream.setListener(stream.getListener());
			if(frame.isEndHeaders()) {
				// If we are done getting headers
				stream.setStatus(RESERVED_REMOTE);
			} else {
				stream.setStatus(WAITING_MORE_NORMAL_HEADERS);
			}
		}

		private void handleContinuation(Http2Continuation frame, Stream stream) {
			HttpMessage msg;

			switch(stream.getStatus()) {
				case WAITING_MORE_PUSH_PROMISE_HEADERS: // after a PUSH_PROMISE
					msg = stream.getRequest();
					break;
				case WAITING_MORE_NORMAL_HEADERS: // after a HEADERS
					msg = stream.getResponse();
					break;
				default:
					// throw, can't get a continuation here
			}

			// Add the headers to the msg

		}

		private void handleWindowUpdate(Http2WindowUpdate frame, Stream stream) {
			// can get this on any stream id
			stream.setWindowIncrement(frame.getWindowSizeIncrement());
		}

		private void handleFrame(Http2Frame frame) {
			if(frame.getFrameType() != SETTINGS && !gotSettings) {
				// TODO: throw here, must get settings as the first frame from the server
			}

			// Transition the stream state
			if(frame.getStreamId() != 0x0) {
				Stream stream = activeStreams.get(frame.getStreamId());
				// TODO: throw here if we don't have a record of this stream

				switch (frame.getFrameType()) {
					case DATA:
						handleData((Http2Data) frame, stream);
						break;
					case HEADERS:
						handleHeaders((Http2Headers) frame, stream);
						break;
					case PRIORITY:
						handlePriority((Http2Priority) frame, stream);
						break;
					case RST_STREAM:
						handleRstStream((Http2RstStream) frame, stream);
						break;
					case PUSH_PROMISE:
						handlePushPromise((Http2PushPromise) frame, stream);
						break;
					case WINDOW_UPDATE:
						handleWindowUpdate((Http2WindowUpdate) frame, stream);
						break;
					case CONTINUATION:
						handleContinuation((Http2Continuation) frame, stream);
						break;
					default:
						// throw a protocol error
				}
			} else {
				switch (frame.getFrameType()) {
					case SETTINGS:
						handleSettings((Http2Settings) frame);
						break;
					case GOAWAY:
						handleGoAway((Http2GoAway) frame);
						break;
					case PING:
						handlePing((Http2Ping) frame);
						break;
					default:
						// Throw a protocol error
				}
			}
		}

		@Override
		public void incomingData(Channel channel, ByteBuffer b) {
			DataWrapper newData = wrapperGen.wrapByteBuffer(b);
			ParserResult parserResult = http2Parser.parse(oldData, newData);

			for(Http2Frame frame: parserResult.getParsedFrames()) {
				handleFrame(frame);
			}
		}

		@Override
		public void farEndClosed(Channel channel) {

		}

		@Override
		public void failure(Channel channel, ByteBuffer data, Exception e) {

		}

		@Override
		public void applyBackPressure(Channel channel) {

		}

		@Override
		public void releaseBackPressure(Channel channel) {

		}
	}

	private class Http11DataListener implements DataListener {
		private boolean processingChunked = false;
		private Memento memento = http11Parser.prepareToParse();

		@Override
		public void incomingData(Channel channel, ByteBuffer b) {
			log.info("size="+b.remaining());
			DataWrapper wrapper = wrapperGen.wrapByteBuffer(b);
			memento = http11Parser.parse(memento, wrapper);

			List<HttpPayload> parsedMessages = memento.getParsedMessages();
			for(HttpPayload msg : parsedMessages) {
				if(processingChunked) {
					HttpChunk chunk = (HttpChunk) msg;
					ResponseListener listener = responsesToComplete.peek();
					if(chunk.isLastChunk()) {
						processingChunked = false;
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
			log.info("far end closed");
			isClosed = true;
			cleanUpPendings("Remote end closed");
			
			if(closeListener != null)
				closeListener.farEndClosed(HttpSocketImpl.this);
		
		}

		@Override
		public void failure(Channel channel, ByteBuffer data, Exception e) {
			log.error("Failure on channel="+channel, e);
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
