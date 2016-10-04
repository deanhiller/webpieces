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
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.*;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.exceptions.NioClosedChannelException;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.handlers.RecordingDataListener;

import static com.webpieces.http2parser.api.dto.Http2FrameType.SETTINGS;
import static com.webpieces.http2parser.api.dto.Http2Settings.Parameter.*;
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

	enum Protocol { HTTP11, HTTP2 };
	private Protocol protocol = HTTP11;

	private ProxyDataListener dataListener;
	private CloseListener closeListener;
	private HttpsSslEngineFactory factory;
	private ChannelManager mgr;
	private String idForLogging;
	private boolean isRecording = true;

	private InetSocketAddress addr;

	// HTTP 2
	private Http2Parser http2Parser;
	private boolean tryHttp2 = true;
	private Map<Http2Settings.Parameter, Integer> localPreferredSettings = new HashMap<>();

	// TODO: Initialize these two with the protocol defaults
	private ConcurrentHashMap<Http2Settings.Parameter, Integer> remoteSettings = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Http2Settings.Parameter, Integer> localSettings = new ConcurrentHashMap<>();

	private ConcurrentHashMap<Integer, Stream> activeStreams = new ConcurrentHashMap<>();

	// start with streamId 3 because 1 might be used for the upgrade stream.
	private int nextStreamId = 0x3;

	// TODO: figure out how to deal with the goaway. For now we're just
	// going to record what they told us.
	private boolean remoteGoneAway = false;
	private int goneAwayLastStreamId;
	private Http2ErrorCode goneAwayErrorCode;
	private DataWrapper additionalDebugData;

	// HTTP 1.1
	private HttpParser httpParser;
	private ConcurrentLinkedQueue<PendingRequest> pendingRequests = new ConcurrentLinkedQueue<>();
	private ConcurrentLinkedQueue<ResponseListener> responsesToComplete = new ConcurrentLinkedQueue<>();



	public HttpSocketImpl(ChannelManager mgr, String idForLogging, HttpsSslEngineFactory factory, HttpParser parser2,
						  Http2Parser http2Parser,
			CloseListener listener) {
		this.factory = factory;
		this.mgr = mgr;
		this.idForLogging = idForLogging;
		this.httpParser = parser2;
		this.http2Parser = http2Parser;
		this.closeListener = listener;
		this.dataListener = new ProxyDataListener();

		// Initialize to defaults
		remoteSettings.put(SETTINGS_HEADER_TABLE_SIZE, 4096);
		localSettings.put(SETTINGS_HEADER_TABLE_SIZE, 4096);

		remoteSettings.put(SETTINGS_ENABLE_PUSH, 1);
		localSettings.put(SETTINGS_ENABLE_PUSH, 1);

		// No limit for MAX_CONCURRENT_STREAMS by default so it isn't in the map

		remoteSettings.put(SETTINGS_INITIAL_WINDOW_SIZE, 65535);
		localSettings.put(SETTINGS_INITIAL_WINDOW_SIZE, 65535);

		remoteSettings.put(SETTINGS_MAX_FRAME_SIZE, 16384);
		localSettings.put(SETTINGS_MAX_FRAME_SIZE, 16384);

		// No limit for MAX_HEADER_LIST_SIZE by default, so not in the map
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
				return negotiateHttpVersion(addr);
			}
			else {
				return CompletableFuture.completedFuture(connected(addr));
			}
		});
		return connectFuture;
	}

	private void sendHttp2Preface() {
		String prefaceString = "505249202a20485454502f322e300d0a0d0a534d0d0a0d0a";
		log.info("sending preface");
		channel.write(ByteBuffer.wrap(DatatypeConverter.parseHexBinary(prefaceString)));
		Http2Settings settingsFrame = new Http2Settings();

		settingsFrame.setSettings(localPreferredSettings);
		log.info("sending settings");
		channel.write(ByteBuffer.wrap(http2Parser.marshal(settingsFrame).createByteArray()));
	}

	private CompletableFuture<HttpSocket> negotiateHttpVersion(InetSocketAddress addr) {
		// First check if ALPN says HTTP2, in which case, set the protocol to HTTP2 and we're done
		if (true) { // let's just try http2 first and see what happens
			protocol = HTTP2;
			dataListener.setProtocol(HTTP2);
			sendHttp2Preface();

			return CompletableFuture.completedFuture(connected(addr));

		} else { // Try the HTTP1.1 upgrade technique
			HttpRequest upgradeRequest = new HttpRequest();
			HttpRequestLine requestLine = new HttpRequestLine();

			// TODO: switch this back to HEAD
			// HEAD doesn't work when connecting to a chunking server
			// because the parser stays in chunked mode because there was no final chunk
			// We have to fix the parser so that HEAD responses that have no chunks don't
			// leave the parser in chunking mode.
			requestLine.setMethod(KnownHttpMethod.GET);
			requestLine.setUri(new HttpUri("/"));
			requestLine.setVersion(new HttpVersion());
			upgradeRequest.setRequestLine(requestLine);
			upgradeRequest.addHeader(new Header("Connection", "Upgrade, HTTP2-Settings"));
			upgradeRequest.addHeader(new Header("Upgrade", "h2c"));
			upgradeRequest.addHeader(new Header("Host", addr.getHostName() + ":" + addr.getPort()));

			Http2Settings settingsFrame = new Http2Settings();
			settingsFrame.setSettings(localPreferredSettings);
			upgradeRequest.addHeader(new Header("HTTP2-Settings",
					Base64.getEncoder().encodeToString(http2Parser.marshal(settingsFrame).createByteArray())));

			CompletableFuture<HttpResponse> response = sendIgnoreConnected(upgradeRequest);
			return response.thenCompose(r -> {
				if(r.getStatusLine().getStatus().getCode() != 101) {
					// That didn't work, let's not try http2 and try again
					tryHttp2 = false;

					// If the response was not chunked, then we're going to assume
					// that the connection will be closed and we have to reconnect without
					// HTTP2. If the response was chunked, then we can just say we're connected
					// and proceed with HTTP1.1.
					// TODO: Find a way to actually just see if the connection has been closed (or is about to be?) or not.
					Header transferEncodingHeader = r.getHeaderLookupStruct().getHeader(KnownHeaderName.TRANSFER_ENCODING);
					if(transferEncodingHeader != null && transferEncodingHeader.getValue().equals("chunked"))
						return CompletableFuture.completedFuture(connected(addr));
					else
						return connect(addr);
				} else {
					protocol = HTTP2;
					dataListener.setProtocol(HTTP2);
					sendHttp2Preface();

					return CompletableFuture.completedFuture(connected(addr));
				}
			});
		}
	}

	private CompletableFuture<HttpResponse> sendIgnoreConnected(HttpRequest request) {
		CompletableFuture<HttpResponse> future = new CompletableFuture<HttpResponse>();
		ResponseListener l = new CompletableListener(future, true);
		actuallySendRequest(request, l);
		return future;
	}

	@Override
	public CompletableFuture<HttpResponse> send(HttpRequest request) {
		CompletableFuture<HttpResponse> future = new CompletableFuture<>();
		ResponseListener l = new CompletableListener(future);
		send(request, l);
		return future;
	}
	
	private synchronized HttpSocket connected(InetSocketAddress addr) {
		connected = true;
		this.addr = addr;
		
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

	private LinkedList<HasHeaders.Header> requestToHeaders(HttpRequest request) {
		HttpRequestLine requestLine = request.getRequestLine();
		List<Header> requestHeaders = request.getHeaders();

		LinkedList<HasHeaders.Header> headerList = new LinkedList<>();

		// Add regular headers
		for(Header header: requestHeaders) {
			headerList.add(new HasHeaders.Header(header.getName().toLowerCase(), header.getValue()));
		}

		// add special headers
		headerList.add(new HasHeaders.Header(":method", requestLine.getMethod().getMethodAsString()));

		UrlInfo urlInfo = requestLine.getUri().getUriBreakdown();

		// Figure out scheme
		if(urlInfo.getPrefix() != null) {
			headerList.add(new HasHeaders.Header(":scheme", urlInfo.getPrefix()));
		} else {
			if(channel.isSslChannel()) {
				headerList.add(new HasHeaders.Header(":scheme", "https"));
			} else {
				headerList.add(new HasHeaders.Header(":scheme", "http"));
			}
		}

		// Figure out authority
		String h = null;
		for(HasHeaders.Header header: headerList) {
			if(header.header.equals("host")) {
				h = header.value;
				break;
			}
		}
		if(h != null) {
			headerList.add(new HasHeaders.Header(":authority", h));
		} else {
			if (urlInfo.getHost() != null) {
				if (urlInfo.getPort() == null)
					headerList.add(new HasHeaders.Header(":authority", urlInfo.getHost()));
				else
					headerList.add(new HasHeaders.Header(":authority", String.format("%s:%d", urlInfo.getHost(), urlInfo.getPort())));
			} else {
				headerList.add(new HasHeaders.Header(":authority", addr.getHostName() + ":" + addr.getPort()));
			}
		}
		headerList.add(new HasHeaders.Header(":path", urlInfo.getFullPath()));

		return headerList;
	}

	private CompletableFuture<Channel> sendDataFrames(DataWrapper body, int streamId, Stream stream) {
		Http2Data newFrame = new Http2Data();
		newFrame.setStreamId(streamId);

		// writes only one frame at a time
		if(body.getReadableSize() <= remoteSettings.get(SETTINGS_MAX_FRAME_SIZE)) {
			newFrame.setData(body);
			newFrame.setEndStream(true);
			log.info("sending final data frame");
			return channel.write(ByteBuffer.wrap(http2Parser.marshal(newFrame).createByteArray())).thenApply(
					channel -> {
						stream.setStatus(HALF_CLOSED_LOCAL);
						return channel;
					}
			);
		} else {
			List<? extends DataWrapper> split = wrapperGen.split(body, remoteSettings.get(SETTINGS_MAX_FRAME_SIZE));
			newFrame.setData(split.get(0));
			log.info("sending non-final data frame");
			return channel.write(ByteBuffer.wrap(http2Parser.marshal(newFrame).createByteArray())).thenCompose(
					channel ->  sendDataFrames(split.get(1), streamId, stream)
			);
		}
	}

	// we never send endstream on the header frame to make our life easier. we always just send
	// endstream on a data frame.
	private CompletableFuture<Channel> sendHeaderFrames(LinkedList<HasHeaders.Header> headerList, int streamId, Stream stream, boolean firstFrame) {
		// Assume for now we can send all the headers in one frame
		// we're going to have to create a 'hasHeaders' interface so we can
		// abstract between Headers and Continuation frames here
		// if firstFrame == true then create Http2Headers, otherwise create Http2Continuation
		List<Http2Frame> frameList = http2Parser.createHeaderFrames(headerList, Http2Headers.class, streamId);

		// If it all fits into one frame
		if(true) {
			log.info("sending final header frame");
			return channel.write(ByteBuffer.wrap(http2Parser.marshal(frameList.get(0)).createByteArray())).thenApply(
					channel ->
					{
						stream.setStatus(OPEN);
						return channel;
					}
			);
		} else {
			// figure out how to split up the headermap into what can fit and what can't.
			log.info("sending non-final header frame");
			return channel.write(ByteBuffer.wrap(http2Parser.marshal(frameList.get(0)).createByteArray())).thenCompose(
					channel -> sendHeaderFrames(new LinkedList<>(), streamId, stream, false)
			);
		}
	}

	private void actuallySendRequest(HttpRequest request, ResponseListener listener) {
		ResponseListener l = new CatchResponseListener(listener);

		if(protocol == HTTP11) {
			ByteBuffer wrap = ByteBuffer.wrap(httpParser.marshalToBytes(request));

			//put this on the queue before the write to be completed from the listener below
			responsesToComplete.offer(l);

			log.info("sending request now. req=" + request.getRequestLine().toString());
			CompletableFuture<Channel> write = channel.write(wrap);
			write.exceptionally(e -> fail(l, e));
		} else { // HTTP2
			// Create a stream
			Stream newStream = new Stream();

			// Find a new Stream id
			activeStreams.put(nextStreamId, newStream);
			nextStreamId += 2;

			LinkedList<HasHeaders.Header> headers = requestToHeaders(request);
			sendHeaderFrames(headers, nextStreamId, newStream, true).thenApply(
					channel -> sendDataFrames(request.getBodyNonNull(), nextStreamId, newStream));
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

		private void receivedEndStream(Stream stream) {
			// Make sure status can accept ES
			switch(stream.getStatus()) {
				case OPEN:
				case HALF_CLOSED_LOCAL:
					stream.setStatus(HALF_CLOSED_REMOTE);

					// Now send ES back
					Http2Data sendFrame = new Http2Data();
					sendFrame.setEndStream(true);

					// Set the stream status to closed after the final ES frame is sent back.
					// we want to keep track somewhere of our window
					log.info("sending endstream ack data frame");
					channel.write(ByteBuffer.wrap(http2Parser.marshal(sendFrame).createByteArray()))
							.thenAccept(channel -> stream.setStatus(CLOSED));
					break;
				default:
					// throw error here
			}
		}

		private void handleData(Http2Data frame, Stream stream) {
			// Only allowable if stream is open or half closed local
			switch(stream.getStatus()) {
				case OPEN:
				case HALF_CLOSED_LOCAL:
					boolean isComplete = frame.isEndStream();
					stream.getListener().incomingData(frame.getData(), stream.getRequest(), isComplete);
					if(isComplete)
						receivedEndStream(stream);
					break;
				default:
					// Throw
			}
		}

		private HttpResponse createResponseFromHeaders(Queue<HasHeaders.Header> headers) {
			HttpResponse response = new HttpResponse();

			// Set special header
			String statusString = null;
			for(HasHeaders.Header header: headers) {
				if (header.header.equals(":status")) {
					statusString = header.value;
					break;
				}
			}
			// TODO: throw if no such header

			HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
			HttpResponseStatus status = new HttpResponseStatus();
			status.setCode(Integer.parseInt(statusString));
			// TODO: throw if can't parse

			statusLine.setStatus(status);
			response.setStatusLine(statusLine);

			// Set all other headers
			for(HasHeaders.Header header: headers) {
				if(header.header.equals(":status"))
					response.addHeader(new Header(header.header, header.value));
			}

			return response;
		}

		private HttpRequest createRequestFromHeaders(Queue<HasHeaders.Header> headers) {
			Map<String, String> headerMap = new HashMap<>();
			for(HasHeaders.Header header: headers) {
				headerMap.put(header.header, header.value);
			}
			HttpRequest request = new HttpRequest();

			// Set special headers
			// TODO: throw if no such headers
			String method = headerMap.get(":method");
			String scheme = headerMap.get(":scheme");
			String authority = headerMap.get(":authority");
			String path = headerMap.get(":path");

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
			for(HasHeaders.Header header: headers) {
				if(!specialHeaders.contains(header.header))
					request.addHeader(new Header(header.header, header.value));
			}

			return request;
		}

		private void handleHeaders(Http2Headers frame, Stream stream) {
			switch (stream.getStatus()) {
				case IDLE:
					break;
				default:
					// throw appropriate error
			}

			// start accumulating headers
			ConcurrentLinkedQueue<HasHeaders.Header> headers = new ConcurrentLinkedQueue<>();
			headers.addAll(frame.getHeaders());
			stream.setHeaderHeaders(headers);

			if(frame.isEndHeaders()) {
				// If we are done getting headers, create the response
				HttpResponse response = createResponseFromHeaders(frame.getHeaders());
				stream.setResponse(response);
				stream.setStatus(OPEN);
				boolean isComplete = frame.isEndStream();
				stream.getListener().incomingResponse(response, stream.getRequest(), isComplete);
				if(isComplete)
					receivedEndStream(stream);
			} else {
				if(frame.isEndStream()) {
					// TODO: throw here, because we can't end the stream when waiting for more headers
				}
				stream.setStatus(WAITING_MORE_NORMAL_HEADERS);
			}
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
				case CLOSED:
				case WAITING_MORE_NORMAL_HEADERS:
				case WAITING_MORE_PUSH_PROMISE_HEADERS:
					// TODO: put the error code in the appropriate exception
					stream.getListener().failure(new RuntimeException("blah"));
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
			// TODO: close all lower numbered even IDLE streams
			activeStreams.put(newStreamId, promisedStream);
			ConcurrentLinkedQueue<HasHeaders.Header> headers = new ConcurrentLinkedQueue<>();
			headers.addAll(frame.getHeaders());
			promisedStream.setPushPromiseHeaders(headers);

			// Uses the same listener as the stream it came in on
			promisedStream.setListener(stream.getListener());

			if(frame.isEndHeaders()) {
				// If we are done getting headers, set the request
				HttpRequest request = createRequestFromHeaders(frame.getHeaders());
				promisedStream.setRequest(request);
				promisedStream.setStatus(RESERVED_REMOTE);
			} else {
				promisedStream.setStatus(WAITING_MORE_NORMAL_HEADERS);
			}
		}

		private void handleContinuation(Http2Continuation frame, Stream stream) {
			ConcurrentLinkedQueue<HasHeaders.Header> headers;

			switch(stream.getStatus()) {
				case WAITING_MORE_PUSH_PROMISE_HEADERS: // after a PUSH_PROMISE
					headers = stream.getPushPromiseHeaders();
					break;
				case WAITING_MORE_NORMAL_HEADERS: // after a HEADERS
					headers = stream.getHeaderHeaders();
					break;
				default:
					// throw, can't get a continuation here, spit out PROTOCOL_ERROR
					throw new RuntimeException("blah");
			}
			// Add the headers to the msg
			// Set all other headers
			headers.addAll(frame.getHeaders());

			if(frame.isEndHeaders()) {
				// If we're done getting headers, add them to the request/response
				stream.setStatus(RESERVED_REMOTE);
				switch(stream.getStatus()) {
					case WAITING_MORE_NORMAL_HEADERS:
						HttpResponse response = createResponseFromHeaders(headers);
						stream.setResponse(response);
						break;
					case WAITING_MORE_PUSH_PROMISE_HEADERS:
						HttpRequest request = createRequestFromHeaders(headers);
						stream.setRequest(request);
						break;
					default:
						// should throw in the prior switch if this is the case
						throw new RuntimeException("should not happen");
				}

				// Send what we got back to the listener
				receivedEndStream(stream);
			}
		}

		private void handleWindowUpdate(Http2WindowUpdate frame, Stream stream) {
			// can get this on any stream id
			stream.setWindowIncrement(frame.getWindowSizeIncrement());
		}

		private void handleSettings(Http2Settings frame) {
			if(frame.isAck()) {
				// we received an ack, so the settings we sent have been accepted.
				for(Map.Entry<Http2Settings.Parameter, Integer> entry: localPreferredSettings.entrySet()) {
					localSettings.put(entry.getKey(), entry.getValue());
				}
			} else {
				// We've received a settings. Update remoteSettings and send an ack
				gotSettings = true;
				for(Map.Entry<Http2Settings.Parameter, Integer> entry: frame.getSettings().entrySet()) {
					remoteSettings.put(entry.getKey(), entry.getValue());
				}
				Http2Settings responseFrame = new Http2Settings();
				responseFrame.setAck(true);
				log.info("sending settings ack");
				channel.write(ByteBuffer.wrap(http2Parser.marshal(responseFrame).createByteArray()));
			}
		}

		// TODO: actually deal with this goaway stuff where necessary
		private void handleGoAway(Http2GoAway frame) {
			remoteGoneAway = true;
			goneAwayLastStreamId = frame.getLastStreamId();
			goneAwayErrorCode = frame.getErrorCode();
			additionalDebugData = frame.getDebugData();
		}

		private void handlePing(Http2Ping frame) {
			if(!frame.isPingResponse()) {
				// Send the same frame back, setting ping response
				frame.setIsPingResponse(true);
				log.info("sending ping response");
				channel.write(ByteBuffer.wrap(http2Parser.marshal(frame).createByteArray()));
			} else {
				// measure latency from the ping that was sent. The opaqueData we sent is
				// System.nanoTime() so we just measure the difference
				long latency = System.nanoTime() - frame.getOpaqueData();
				log.info("Ping: %ld ns", latency);
			}
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
				log.info("got frame="+frame);
				handleFrame(frame);
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

	private class Http11DataListener implements DataListener {
		private boolean processingChunked = false;
		private Memento memento = httpParser.prepareToParse();

		@Override
		public void incomingData(Channel channel, ByteBuffer b) {
			log.info("size="+b.remaining());
			DataWrapper wrapper = wrapperGen.wrapByteBuffer(b);
			memento = httpParser.parse(memento, wrapper);

			List<HttpPayload> parsedMessages = memento.getParsedMessages();
			for(HttpPayload msg : parsedMessages) {
				if(processingChunked) {
					HttpChunk chunk = (HttpChunk) msg;
					ResponseListener listener = responsesToComplete.peek();
					if(chunk.isLastChunk()) {
						processingChunked = false;
						responsesToComplete.poll();
					}
					
					listener.incomingData(chunk.getBodyNonNull(), chunk.isLastChunk());
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
