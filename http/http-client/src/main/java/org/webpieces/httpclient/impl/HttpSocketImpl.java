package org.webpieces.httpclient.impl;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntUnaryOperator;

import javax.net.ssl.SSLEngine;
import javax.xml.bind.DatatypeConverter;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
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
import org.webpieces.httpclient.api.exceptions.*;
import org.webpieces.httpclient.api.exceptions.InternalError;
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

import static com.webpieces.http2parser.api.dto.Http2FrameType.HEADERS;
import static com.webpieces.http2parser.api.dto.Http2FrameType.SETTINGS;
import static com.webpieces.http2parser.api.dto.Http2Settings.Parameter.*;
import static java.lang.Math.min;
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
	private AtomicBoolean tryHttp2 = new AtomicBoolean(true);
    private AtomicBoolean negotiationDone = new AtomicBoolean(false);
    private AtomicBoolean negotiationStarted = new AtomicBoolean(false);
    private CompletableFuture<Channel> negotiationDoneNotifier = new CompletableFuture<>();
	private Map<Http2Settings.Parameter, Integer> localPreferredSettings = new HashMap<>();

	private ConcurrentHashMap<Http2Settings.Parameter, Integer> remoteSettings = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Http2Settings.Parameter, Integer> localSettings = new ConcurrentHashMap<>();

	private ConcurrentHashMap<Integer, Stream> activeStreams = new ConcurrentHashMap<>();
    private AtomicInteger nextStreamId = new AtomicInteger(0x1);
    private ConcurrentHashMap<Integer, AtomicInteger> outgoingFlowControl = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, AtomicInteger> incomingFlowControl = new ConcurrentHashMap<>();

	private Encoder encoder;
	private Decoder decoder;
	private AtomicBoolean maxHeaderTableSizeNeedsUpdate = new AtomicBoolean(false);
	private AtomicInteger minimumMaxHeaderTableSizeUpdate = new AtomicInteger(Integer.MAX_VALUE);

	// TODO: figure out how to deal with the goaway. For now we're just
	// going to record what they told us.
	private AtomicBoolean remoteGoneAway = new AtomicBoolean(false);
	private AtomicInteger goneAwayLastStreamId = new AtomicInteger(0x0);
	private AtomicReference<Http2ErrorCode> goneAwayErrorCode = new AtomicReference<>(Http2ErrorCode.NO_ERROR);
	private AtomicReference<DataWrapper> additionalDebugData = new AtomicReference<>(wrapperGen.emptyWrapper());

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

        this.decoder = new Decoder(4096, localSettings.get(SETTINGS_HEADER_TABLE_SIZE));
        this.encoder = new Encoder(remoteSettings.get(SETTINGS_HEADER_TABLE_SIZE));
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
		
		connectFuture = channel.connect(addr, dataListenerToUse).thenApply(channel -> connected(addr));
		return connectFuture;
	}

	private void sendHttp2Preface() {
		String prefaceString = "505249202a20485454502f322e300d0a0d0a534d0d0a0d0a";
		log.info("sending preface");
		channel.write(ByteBuffer.wrap(DatatypeConverter.parseHexBinary(prefaceString)));
		Http2Settings settingsFrame = new Http2Settings();

		settingsFrame.setSettings(localPreferredSettings);
		log.info("sending settings: " + settingsFrame);
		channel.write(ByteBuffer.wrap(http2Parser.marshal(settingsFrame).createByteArray()));
	}

	private void enableHttp2() {
        protocol = HTTP2;
        dataListener.setProtocol(HTTP2);
        sendHttp2Preface();
        negotiationDone.set(true);

        // Initialize connection level flow control
        initializeFlowControl(0x0);

        Timer timer = new Timer();
        // in 5 seconds send a ping every 5 seconds
        timer.schedule(new SendPing(), 5000, 5000);

    }

	private CompletableFuture<Channel> negotiateHttpVersion(HttpRequest req, ResponseListener listener) {
		// First check if ALPN says HTTP2, in which case, set the protocol to HTTP2 and we're done
        // If we set this to true, then everyting is copacetic with http://nghttp2.org, but
        // if we set it to false and do upgrade negotation, then the first request succeeds
        // but subsequent requests give us RstStream responses from the server.
		if (true) { // We don't know how to check ALPN yet, but if we do, put that check here
            log.info("setting http2 because of alpn");
            enableHttp2();
            negotiationDone.set(true);
            negotiationDoneNotifier.complete(channel);
			return CompletableFuture.completedFuture(channel);

		} else { // Try the HTTP1.1 upgrade technique
            log.info("attempting http11 upgrade");
			req.addHeader(new Header("connection", "Upgrade, HTTP2-Settings"));
			req.addHeader(new Header("upgrade", "h2c"));
			Http2Settings settingsFrame = new Http2Settings();
			settingsFrame.setSettings(localPreferredSettings);
            // For some reason we need to add a " " after the base64urlencoded settings to get this to work
            // against nghttp2.org ?
			req.addHeader(new Header("http2-settings",
					Base64.getUrlEncoder().encodeToString(http2Parser.marshal(settingsFrame).createByteArray()) + " "));

            CompletableFuture<HttpResponse> response = sendHttp11AndWaitForHeaders(req);

			return response.thenApply(r -> {
				if(r.getStatusLine().getStatus().getCode() != 101) {
                    log.info("upgrade failed");
					// That didn't work, let's not try http2 and send what we have so far to the normal listener
					tryHttp2.set(false);
                    negotiationDone.set(true);
                    negotiationDoneNotifier.complete(channel);

                    // If the response is chunked then it is probably not complete.
                    // TODO: make sure this is right. would be nicer to grab the isComplete
                    // out of the incomingResponse call to the CompletableListener in
                    // sendHttp11AndWaitForHeaders I think.
                    listener.incomingResponse(r, !r.isHasChunkedTransferHeader());
                    return channel;
				} else {
                    log.info("upgrade suceeded");
                    enableHttp2();

                    int initialStreamId = getAndIncrementStreamId();
                    Stream initialStream = new Stream();
                    initialStream.setStreamId(initialStreamId);
                    initializeFlowControl(initialStreamId);
                    initialStream.setRequest(req);
                    initialStream.setListener(listener);
                    initialStream.setResponse(r);
                    // Since we already sent the entire request as the upgrade, the stream basically starts in
                    // half closed local
                    initialStream.setStatus(HALF_CLOSED_LOCAL);
                    activeStreams.put(initialStreamId, initialStream);

                    DataWrapper responseBody = r.getBodyNonNull();

                    // Send the content of the response to the datalistener, if any
                    // Not likely to happen but just in case
                    if(responseBody.getReadableSize() > 0)
                        dataListener.incomingData(channel, ByteBuffer.wrap(responseBody.createByteArray()));

                    // Grab the leftover data out of the http11 parser and send that to the datalistener
                    DataWrapper leftOverData = ((Http11DataListener) dataListener.dataListenerMap.get(HTTP11))
                            .getLeftOverData();
                    if(leftOverData.getReadableSize() > 0)
                        dataListener.incomingData(channel, ByteBuffer.wrap(leftOverData.createByteArray()));

					return channel;
				}
			});
		}
	}

	private CompletableFuture<HttpResponse> sendHttp11AndWaitForHeaders(HttpRequest request) {
		CompletableFuture<HttpResponse> future = new CompletableFuture<HttpResponse>();
		ResponseListener l = new CompletableListener(future, true);
		sendHttp11Request(request, l);
		return future;
	}

	@Override
	public CompletableFuture<HttpResponse> send(HttpRequest request) {
		CompletableFuture<HttpResponse> future = new CompletableFuture<>();
		ResponseListener l = new CompletableListener(future);
		send(request, l);
		return future;
	}

	// Sends the pending requests serially. This way we won't send the second request
    // until the first request has completed http2 negotiation.
	private CompletableFuture<Boolean> clearPendingRequests() {
        if(!pendingRequests.isEmpty()) {
            PendingRequest req = pendingRequests.remove();
            return negotiateAndSendRequest(req.getRequest(), req.getListener()).thenApply(channel -> true)
                    .thenCompose(channel -> {
                        req.complete();
                        return clearPendingRequests();
                    });
        } else {
            return CompletableFuture.completedFuture(false);
        }
    }
	private synchronized HttpSocket connected(InetSocketAddress addr) {
		connected = true;
		this.addr = addr;

        clearPendingRequests();
		
		return this;
	}

	@Override
	public CompletableFuture<HttpRequest> send(HttpRequest request, ResponseListener listener) {
		if(connectFuture == null) 
			throw new IllegalArgumentException("You must at least call httpSocket.connect first(it "
					+ "doesn't have to complete...you just have to call it before caling send)");
        CompletableFuture<HttpRequest> future = new CompletableFuture<>();

		boolean wasConnected = false;
		synchronized (this) {
			if(!connected) {
				pendingRequests.add(new PendingRequest(request, listener, future));
			} else
				wasConnected = true;
		}
		
		if(wasConnected) 
			return negotiateAndSendRequest(request, listener).thenApply(channel -> request);
        else
            return future;
	}

	private LinkedList<HasHeaderFragment.Header> requestToHeaders(HttpRequest request) {
		HttpRequestLine requestLine = request.getRequestLine();
		List<Header> requestHeaders = request.getHeaders();

		LinkedList<HasHeaderFragment.Header> headerList = new LinkedList<>();


		// add special headers
		headerList.add(new HasHeaderFragment.Header(":method", requestLine.getMethod().getMethodAsString()));

		UrlInfo urlInfo = requestLine.getUri().getUriBreakdown();
        headerList.add(new HasHeaderFragment.Header(":path", urlInfo.getFullPath()));

		// Figure out scheme
		if(urlInfo.getPrefix() != null) {
			headerList.add(new HasHeaderFragment.Header(":scheme", urlInfo.getPrefix()));
		} else {
			if(channel.isSslChannel()) {
				headerList.add(new HasHeaderFragment.Header(":scheme", "https"));
			} else {
				headerList.add(new HasHeaderFragment.Header(":scheme", "http"));
			}
		}

		// Figure out authority
		String h = null;
		for(HasHeaderFragment.Header header: headerList) {
			if(header.header.equals("host")) {
				h = header.value;
				break;
			}
		}
		if(h != null) {
			headerList.add(new HasHeaderFragment.Header(":authority", h));
		} else {
			if (urlInfo.getHost() != null) {
				if (urlInfo.getPort() == null)
					headerList.add(new HasHeaderFragment.Header(":authority", urlInfo.getHost()));
				else
					headerList.add(new HasHeaderFragment.Header(":authority", String.format("%s:%d", urlInfo.getHost(), urlInfo.getPort())));
			} else {
				headerList.add(new HasHeaderFragment.Header(":authority", addr.getHostName() + ":" + addr.getPort()));
			}
		}

        // Add regular headers
        for(Header header: requestHeaders) {
            headerList.add(new HasHeaderFragment.Header(header.getName().toLowerCase(), header.getValue()));
        }

		return headerList;
	}

	private CompletableFuture<Channel> sendDataFrames(DataWrapper body, int streamId, Stream stream) {
		Http2Data newFrame = new Http2Data();
		newFrame.setStreamId(streamId);

		// writes only one frame at a time.
		if(body.getReadableSize() <= remoteSettings.get(SETTINGS_MAX_FRAME_SIZE)) {
			// the body fits within one frame so send an endstream with this frame
			newFrame.setData(body);
			newFrame.setEndStream(true);
			log.info("sending final data frame: " + newFrame);
			return channel.write(ByteBuffer.wrap(http2Parser.marshal(newFrame).createByteArray())).thenApply(
					channel -> {
						stream.setStatus(HALF_CLOSED_LOCAL);
						return channel;
					}
			);
		} else {
			// to big, split it, send, and recurse.
			List<? extends DataWrapper> split = wrapperGen.split(body, remoteSettings.get(SETTINGS_MAX_FRAME_SIZE));
			newFrame.setData(split.get(0));
			log.info("sending non-final data frame: " + newFrame);
			return channel.write(ByteBuffer.wrap(http2Parser.marshal(newFrame).createByteArray())).thenCompose(
					channel ->  sendDataFrames(split.get(1), streamId, stream)
			);
		}
	}

	// we never send endstream on the header frame to make our life easier. we always just send
	// endstream on a data frame.
	private CompletableFuture<Channel> sendHeaderFrames(LinkedList<HasHeaderFragment.Header> headerList, int streamId, Stream stream) {

		// If the header table size needs update, we pre-fill the buffer with the update notification
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			if (maxHeaderTableSizeNeedsUpdate.get()) {
				// If we need to update the max header table size
				int newMaxHeaderTableSize = remoteSettings.get(SETTINGS_HEADER_TABLE_SIZE);
				if (minimumMaxHeaderTableSizeUpdate.get() < newMaxHeaderTableSize) {
					encoder.setMaxHeaderTableSize(out, minimumMaxHeaderTableSizeUpdate.get());
				}
				encoder.setMaxHeaderTableSize(out, newMaxHeaderTableSize);
				minimumMaxHeaderTableSizeUpdate.set(Integer.MAX_VALUE);
				maxHeaderTableSizeNeedsUpdate.set(false);
			}
		} catch (IOException e) {
            // TODO: Remove debugdata when not in developer mode
			throw new InternalError(lastClosedServerStream().orElse(0), wrapperGen.wrapByteArray(e.toString().getBytes()));
		}

		// if firstFrame == true then create Http2Headers, otherwise create Http2Continuation
		List<Http2Frame> frameList = http2Parser.createHeaderFrames(headerList, HEADERS, streamId, remoteSettings, encoder, out);

		// Send all the frames at once
		log.info("sending header frames: " + frameList);
		return channel.write(ByteBuffer.wrap(http2Parser.marshal(frameList).createByteArray())).thenApply(
				channel ->
				{
					stream.setStatus(OPEN);
					return channel;
				}
		);
	}

	private long countOpenServerStreams() {
        return activeStreams.entrySet().stream().filter(entry -> {
            Stream.StreamStatus status = entry.getValue().getStatus();
            boolean open = (Arrays.asList(OPEN, HALF_CLOSED_LOCAL, HALF_CLOSED_REMOTE).contains(status));
            boolean server = entry.getValue().getStreamId() % 2 == 0;
            return open && server;
        }).count();
    }

    private long countOpenClientStreams() {
        return activeStreams.entrySet().stream().filter(entry -> {
            Stream.StreamStatus status = entry.getValue().getStatus();
            boolean open = (Arrays.asList(OPEN, HALF_CLOSED_LOCAL, HALF_CLOSED_REMOTE).contains(status));
            boolean client = entry.getValue().getStreamId() % 2 == 1;
            return open && client;
        }).count();
    }

    private Optional<Integer> lastClosedServerStream() {
        return activeStreams.entrySet()
                .stream()
                .filter(entry -> (entry.getValue().getStatus() == CLOSED) && (entry.getValue().getStreamId() % 2 == 0))
                .max(Comparator.comparingInt(Map.Entry::getKey)).map(entry -> entry.getKey());
    }

    private Optional<Integer> lastClosedClientStream() {
        return activeStreams.entrySet()
                .stream()
                .filter(entry -> (entry.getValue().getStatus() == CLOSED) && (entry.getValue().getStreamId() % 2 == 1))
                .max(Comparator.comparingInt(Map.Entry::getKey)).map(entry -> entry.getKey());
    }

	private int getAndIncrementStreamId() {
        return nextStreamId.getAndAdd(2);
	}

	private class SendPing extends TimerTask {
        public void run() {
            Http2Ping pingFrame = new Http2Ping();
            pingFrame.setOpaqueData(System.nanoTime());
            channel.write(ByteBuffer.wrap(http2Parser.marshal(pingFrame).createByteArray()));
        }
    }

    private CompletableFuture<Channel> sendHttp11Request(HttpRequest request, ResponseListener l) {
        ByteBuffer wrap = ByteBuffer.wrap(httpParser.marshalToBytes(request));

        //put this on the queue before the write to be completed from the listener below
        responsesToComplete.offer(l);

        log.info("sending request now. req=" + request);
        CompletableFuture<Channel> write = channel.write(wrap);
        return write.exceptionally(e -> fail(l, e));
    }

	private CompletableFuture<Channel> negotiateAndSendRequest(HttpRequest request, ResponseListener listener) {
        ResponseListener l = new CatchResponseListener(listener);
        if (!negotiationDone.get()) {
            if (!negotiationStarted.get()) {
                negotiationStarted.set(true);
                return negotiateHttpVersion(request, l);
            } else {
                log.info("waiting for negotiation to complete");
                return negotiationDoneNotifier.thenCompose(channel -> {
                    log.info("done waiting for negotiation to complete");
                    return actuallySendRequest(request, l);
                });
            }
        } else {
            log.info("not waiting for negotiation at all");
            return actuallySendRequest(request, l);
        }
    }

    private void initializeFlowControl(int streamId) {
        // Set up flow control
        incomingFlowControl.put(streamId, new AtomicInteger(localSettings.get(SETTINGS_INITIAL_WINDOW_SIZE)));
        outgoingFlowControl.put(streamId, new AtomicInteger(remoteSettings.get(SETTINGS_INITIAL_WINDOW_SIZE)));
    }

    private CompletableFuture<Channel> sendHttp2Request(HttpRequest request, ResponseListener l) {
        // Check if we are allowed to create a new stream
        if (remoteSettings.containsKey(SETTINGS_MAX_CONCURRENT_STREAMS) &&
                countOpenClientStreams() >= remoteSettings.get(SETTINGS_MAX_CONCURRENT_STREAMS)) {
            throw new ClientError("Max concurrent streams exceeded, please wait and try again.");
            // TODO: create a request queue that gets emptied when there are open streams
        }
        // Create a stream
        Stream newStream = new Stream();

        // Find a new Stream id
        int thisStreamId = getAndIncrementStreamId();
        newStream.setListener(l);
        newStream.setStreamId(thisStreamId);
        newStream.setRequest(request);
        initializeFlowControl(thisStreamId);
        activeStreams.put(thisStreamId, newStream);
        LinkedList<HasHeaderFragment.Header> headers = requestToHeaders(request);
        return sendHeaderFrames(headers, thisStreamId, newStream).thenCompose(
                channel -> sendDataFrames(request.getBodyNonNull(), thisStreamId, newStream));

    }

    private CompletableFuture<Channel> actuallySendRequest(HttpRequest request, ResponseListener l) {
        if (protocol == HTTP11) {
            return sendHttp11Request(request, l);
        } else { // HTTP2
            return sendHttp2Request(request, l);
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
		private AtomicBoolean gotSettings = new AtomicBoolean(false);

		private void receivedEndStream(Stream stream) {
			// Make sure status can accept ES
			switch(stream.getStatus()) {
				case OPEN:
				    stream.setStatus(HALF_CLOSED_REMOTE);
                    break;
				case HALF_CLOSED_LOCAL:
					stream.setStatus(CLOSED);
					break;
				default:
					throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, stream.getStreamId());
			}
		}

		private void decrementIncomingWindow(int streamId, int length) {
            log.info("decrementing window for {} by {}", streamId, length);
            if(incomingFlowControl.get(0x0).addAndGet(- length) < 0) {
                throw new GoAwayError(lastClosedServerStream().orElse(0), Http2ErrorCode.FLOW_CONTROL_ERROR,
                        wrapperGen.emptyWrapper());
            }
            if(incomingFlowControl.get(streamId).decrementAndGet() < 0) {
                throw new RstStreamError(Http2ErrorCode.FLOW_CONTROL_ERROR, streamId);
            }

        }

        private void incrementIncomingWindow(int streamId, int length) {
            log.info("incrementing window for {} by {}", streamId, length);
            incomingFlowControl.get(0x0).addAndGet(length);
            incomingFlowControl.get(streamId).addAndGet(length);

            Http2WindowUpdate frame = new Http2WindowUpdate();
            frame.setWindowSizeIncrement(length);
            frame.setStreamId(0x0);
            channel.write(ByteBuffer.wrap(http2Parser.marshal(frame).createByteArray()));

            // reusing the frame! ack.
            frame.setStreamId(streamId);
            channel.write(ByteBuffer.wrap(http2Parser.marshal(frame).createByteArray()));
        }

		private void handleData(Http2Data frame, Stream stream) {
			// Only allowable if stream is open or half closed local
			switch(stream.getStatus()) {
				case OPEN:
				case HALF_CLOSED_LOCAL:
					boolean isComplete = frame.isEndStream();
                    int payloadLength = http2Parser.getFrameLength(frame);
                    decrementIncomingWindow(frame.getStreamId(), payloadLength);
                    stream.getListener().incomingData(frame.getData(), stream.getRequest(), isComplete).thenAccept(
                            length -> incrementIncomingWindow(frame.getStreamId(), payloadLength));
					if(isComplete)
						receivedEndStream(stream);
					break;
				default:
					throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, stream.getStreamId());
			}
		}

		private HttpResponse createResponseFromHeaders(Queue<HasHeaderFragment.Header> headers, Stream stream) {
			HttpResponse response = new HttpResponse();

            // TODO: throw if special headers are not at the front
			// Set special header
			String statusString = null;
			for(HasHeaderFragment.Header header: headers) {
				if (header.header.equals(":status")) {
					statusString = header.value;
					break;
				}
			}
			if(statusString == null)
			    throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, stream.getStreamId());

			HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
			HttpResponseStatus status = new HttpResponseStatus();
            try {
                status.setCode(Integer.parseInt(statusString));
            } catch(NumberFormatException e) {
                throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, stream.getStreamId());
            }

			statusLine.setStatus(status);
			response.setStatusLine(statusLine);

			// Set all other headers
			for(HasHeaderFragment.Header header: headers) {
				if(header.header.equals(":status"))
					response.addHeader(new Header(header.header, header.value));
			}

			return response;
		}

		private HttpRequest createRequestFromHeaders(Queue<HasHeaderFragment.Header> headers, Stream stream) {
			Map<String, String> headerMap = new HashMap<>();
			for(HasHeaderFragment.Header header: headers) {
				headerMap.put(header.header, header.value);
			}
			HttpRequest request = new HttpRequest();

			// Set special headers
            // TODO: throw if special headers are not at the front
			String method = headerMap.get(":method");
			String scheme = headerMap.get(":scheme");
			String authority = headerMap.get(":authority");
			String path = headerMap.get(":path");
            if(method == null || scheme == null || authority == null || path == null)
                throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, stream.getStreamId());

			// See https://svn.tools.ietf.org/svn/wg/httpbis/specs/rfc7230.html#asterisk-form
			if(method.toLowerCase().equals("options") && path.equals("*")) {
				path = "";
			}

			HttpRequestLine requestLine = new HttpRequestLine();
			requestLine.setUri(new HttpUri(String.format("%s://%s%s", scheme, authority, path)));
			requestLine.setMethod(new HttpRequestMethod(method));
			request.setRequestLine(requestLine);

			List<String> specialHeaders = Arrays.asList(":method", ":scheme", ":authority", ":path");

			// Set all other headers
			for(HasHeaderFragment.Header header: headers) {
				if(!specialHeaders.contains(header.header))
					request.addHeader(new Header(header.header, header.value));
			}

			return request;
		}

		private void handleHeaders(Http2Headers frame, Stream stream) {
			switch (stream.getStatus()) {
				case IDLE:
                    stream.setStatus(OPEN);
                    break;
                case HALF_CLOSED_LOCAL:
                    // No status change in this case
					break;
                case RESERVED_REMOTE:
                    stream.setStatus(HALF_CLOSED_LOCAL);
                    break;
				default:
					throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, stream.getStreamId());
			}

			if(frame.isPriority()) {
                stream.setPriorityDetails(frame.getPriorityDetails());
            }

			if(frame.isEndHeaders()) {
				// the parser has already accumulated the headers in the frame for us.
				HttpResponse response = createResponseFromHeaders(frame.getHeaderList(), stream);
				stream.setResponse(response);

				boolean isComplete = frame.isEndStream();
				if (isComplete)
					receivedEndStream(stream);
			}
			else {
				throw new InternalError(lastClosedServerStream().orElse(0), wrapperGen.emptyWrapper());
			}
		}


		private void handlePriority(Http2Priority frame, Stream stream) {
            // Can be received in any state. We aren't doing anything with this right now.
            stream.setPriorityDetails(frame.getPriorityDetails());
		}

		private void handleRstStream(Http2RstStream frame, Stream stream) {
			switch(stream.getStatus()) {
				case OPEN:
				case HALF_CLOSED_REMOTE:
				case HALF_CLOSED_LOCAL:
				case RESERVED_LOCAL:
				case RESERVED_REMOTE:
				case CLOSED:
					stream.getListener().failure(new RstStreamError(frame.getErrorCode(), stream.getStreamId()));
					stream.setStatus(CLOSED);
					break;
				default:
				    throw new RstStreamError(Http2ErrorCode.PROTOCOL_ERROR, stream.getStreamId());
			}
		}

		private void handlePushPromise(Http2PushPromise frame, Stream stream) {
			// Can get this on any stream id, creates a new stream
			if(frame.isEndHeaders()) {
                if(localSettings.containsKey(SETTINGS_MAX_CONCURRENT_STREAMS) &&
                        countOpenServerStreams() >= localSettings.get(SETTINGS_MAX_CONCURRENT_STREAMS)) {
                    throw new RstStreamError(Http2ErrorCode.REFUSED_STREAM, frame.getPromisedStreamId());
                }
				Stream promisedStream = new Stream();
				int newStreamId = frame.getPromisedStreamId();
                initializeFlowControl(newStreamId);
                promisedStream.setStreamId(newStreamId);

				// TODO: make sure streamid is valid
				// TODO: close all lower numbered even IDLE streams
				activeStreams.put(newStreamId, promisedStream);

				// Uses the same listener as the stream it came in on
				promisedStream.setListener(stream.getListener());
				HttpRequest request = createRequestFromHeaders(frame.getHeaderList(), promisedStream);
				promisedStream.setRequest(request);
				promisedStream.setStatus(RESERVED_REMOTE);
			} else {
				throw new InternalError(lastClosedServerStream().orElse(0), wrapperGen.emptyWrapper());
			}
		}

		private void handleWindowUpdate(Http2WindowUpdate frame, Stream stream) {
			// can get this on any stream id, or with no stream
			//stream.setWindowIncrement(frame.getWindowSizeIncrement());
		}

		private void handleSettings(Http2Settings frame) {
			if(frame.isAck()) {
				// we received an ack, so the settings we sent have been accepted.
				for(Map.Entry<Http2Settings.Parameter, Integer> entry: localPreferredSettings.entrySet()) {
					localSettings.put(entry.getKey(), entry.getValue());
				}
			} else {
				// We've received a settings. Update remoteSettings and send an ack
                gotSettings.set(true);
				for(Map.Entry<Http2Settings.Parameter, Integer> entry: frame.getSettings().entrySet()) {
					remoteSettings.put(entry.getKey(), entry.getValue());
				}

				// What do we do when certain settings are updated
				if(frame.getSettings().containsKey(SETTINGS_HEADER_TABLE_SIZE)) {
					maxHeaderTableSizeNeedsUpdate.set(true);
                    class UpdateMinimum implements IntUnaryOperator {
                        int newTableSize;

                        public UpdateMinimum(int newTableSize) {
                            this.newTableSize = newTableSize;
                        }

                        @Override
                        public int applyAsInt(int operand) {
                            return min(operand, newTableSize);
                        }
                    }
					minimumMaxHeaderTableSizeUpdate.updateAndGet(
					        new UpdateMinimum(frame.getSettings().get(SETTINGS_HEADER_TABLE_SIZE)));
				}
				Http2Settings responseFrame = new Http2Settings();
				responseFrame.setAck(true);
				log.info("sending settings ack: " + responseFrame);
				channel.write(ByteBuffer.wrap(http2Parser.marshal(responseFrame).createByteArray()));
			}
		}

		// TODO: actually deal with this goaway stuff where necessary
		private void handleGoAway(Http2GoAway frame) {
			remoteGoneAway.set(true);
			goneAwayLastStreamId.set(frame.getLastStreamId());
			goneAwayErrorCode.set(frame.getErrorCode());
			additionalDebugData.set(frame.getDebugData());
            farEndClosed(channel);
		}

		private void handlePing(Http2Ping frame) {
			if(!frame.isPingResponse()) {
				// Send the same frame back, setting ping response
				frame.setIsPingResponse(true);
				log.info("sending ping response: " + frame);
				channel.write(ByteBuffer.wrap(http2Parser.marshal(frame).createByteArray()));
			} else {
				// measure latency from the ping that was sent. The opaqueData we sent is
				// System.nanoTime() so we just measure the difference
				long latency = System.nanoTime() - frame.getOpaqueData();
				log.info("Ping: {} ms", latency * 1e-6);
			}
		}

		private void handleFrame(Http2Frame frame) {
			if(frame.getFrameType() != SETTINGS && !gotSettings.get()) {
                throw new GoAwayError(lastClosedServerStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR, wrapperGen.emptyWrapper());
			}

			// Transition the stream state
			if(frame.getStreamId() != 0x0) {
				Stream stream = activeStreams.get(frame.getStreamId());
				// If the stream doesn't exist, create it, but we will drop
                // everything because we don't have a listener for it
                // TODO: make sure the lack of listener doesn't cause problems
                if(stream == null) {
                    stream = new Stream();
                    stream.setStreamId(frame.getStreamId());
                    initializeFlowControl(stream.getStreamId());
                }

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
						throw new InternalError(lastClosedServerStream().orElse(0), wrapperGen.emptyWrapper());
					default:
						throw new GoAwayError(lastClosedServerStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR,
                                wrapperGen.emptyWrapper());
				}
			} else {
				switch (frame.getFrameType()) {
					case WINDOW_UPDATE:
						handleWindowUpdate((Http2WindowUpdate) frame, null);
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
						throw new GoAwayError(lastClosedServerStream().orElse(0), Http2ErrorCode.PROTOCOL_ERROR,
                                    wrapperGen.emptyWrapper());
				}
			}
		}

		@Override
		public void incomingData(Channel channel, ByteBuffer b) {
			DataWrapper newData = wrapperGen.wrapByteBuffer(b);
			ParserResult parserResult = http2Parser.parse(oldData, newData, decoder);

			for(Http2Frame frame: parserResult.getParsedFrames()) {
				log.info("got frame="+frame);
                try {
                    handleFrame(frame);
                } catch (Http2Error e) {
                    channel.write(ByteBuffer.wrap(http2Parser.marshal(e.toFrame()).createByteArray()));
                    if(RstStreamError.class.isInstance(e)) {
                        // Mark the stream closed
                        activeStreams.get(((RstStreamError) e).getStreamId()).setStatus(CLOSED);
                    }
                    if(GoAwayError.class.isInstance(e)) {
                        // TODO: Shut this connection down
                    }
                }
			}
			oldData = parserResult.getMoreData();
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

        /**
         * This is a special 'reach-in' method to let the http2 parser grab the data from the http11
         * parser that has not yet been parsed.
         *
         * @return
         */
        public DataWrapper getLeftOverData() {
           return memento.getLeftOverData();
        }

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
