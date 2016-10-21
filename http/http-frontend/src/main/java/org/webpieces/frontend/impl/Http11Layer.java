package org.webpieces.frontend.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpServerSocket;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.httpcommon.api.exceptions.HttpClientException;
import org.webpieces.httpcommon.api.exceptions.HttpException;
import org.webpieces.httpcommon.impl.Http2EngineImpl;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.httpparser.api.ParseException;
import org.webpieces.httpparser.api.UnparsedState;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.*;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import javax.xml.bind.DatatypeConverter;

import static org.webpieces.httpparser.api.dto.HttpRequest.HttpScheme.HTTPS;

public class Http11Layer {

	private static final DataWrapperGenerator generator = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private HttpParser parser;
	private TimedListener listener;
	private FrontendConfig config;
	private static final Logger log = LoggerFactory.getLogger(Http2EngineImpl.class);

	Http11Layer(HttpParser parser2, TimedListener listener, FrontendConfig config) {
		this.parser = parser2;
		this.listener = listener;
		this.config = config;
	}

	void deserialize(Channel channel, ByteBuffer chunk) {
		List<HttpRequest> parsedRequests = doTheWork(channel, chunk);

        // TODO: if we get chunks, send these to incomingData.. right now we don't support receiving chunks on the server side.
		for(HttpRequest req : parsedRequests) {
			// Check for an HTTP2 upgrade, if not SSL
			if(!channel.isSslChannel()) {
                List<Header> reqHeaders = req.getHeaders();
                String upgradeHeader = null;
                ByteBuffer settingsFrame = null;

                for (Header header : reqHeaders) {
                    if (header.getKnownName() == KnownHeaderName.UPGRADE) {
                        upgradeHeader = header.getValue();
                    }
                    if (header.getKnownName() == KnownHeaderName.HTTP2_SETTINGS) {
                        settingsFrame = ByteBuffer.wrap(DatatypeConverter.parseBase64Binary(header.getValue()));
                    }
                }
                if (upgradeHeader != null && upgradeHeader.toLowerCase().equals("h2c")) {
                    final Optional<ByteBuffer> maybeSettingsFrame = Optional.of(settingsFrame);

                    // Create the upgrade response
                    HttpResponse response = new HttpResponse();
                    HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
                    HttpResponseStatus status = new HttpResponseStatus();
                    status.setKnownStatus(KnownStatusCode.HTTP_101_SWITCHING_PROTOCOLS);
                    statusLine.setStatus(status);
                    response.setStatusLine(statusLine);
                    response.addHeader(new Header("Connection", "Upgrade"));
                    response.addHeader(new Header("Upgrade", "h2c"));

                    // Send the upgrade accept response and then switch to HTTP2
                    getResponseSenderForChannel(channel).sendResponse(response, req, new RequestId(0), true).thenAccept(
                            responseId -> {
                                // Switch the socket to using HTTP2
                                HttpServerSocket socket = getHttpServerSocketForChannel(channel);
                                socket.upgradeHttp2(maybeSettingsFrame);

                                // Send the request to listener (requestid is 1 for this first request)
                                listener.incomingRequest(req, new RequestId(0x1), true, socket.getResponseSender());
                            }
                    );

                    // Drop all subsequent requests?
                    break;
                }
			}

			if(req.isHasChunkedTransferHeader())
				throw new UnsupportedOperationException();
			else
				listener.incomingRequest(req, new RequestId(0), true, getResponseSenderForChannel(channel));

		}
	}

	private List<HttpRequest> doTheWork(Channel channel, ByteBuffer chunk) {
		ChannelSession session = channel.getSession();		
		Memento memento = (Memento) session.get("memento");
		
		if(memento == null) {
			memento = parser.prepareToParse();
			session.put("memento", memento);
		}

		DataWrapper dataWrapper = generator.wrapByteBuffer(chunk);
		
		Memento resultMemento = parse(memento, dataWrapper);

		List<HttpPayload> parsedMsgs = resultMemento.getParsedMessages();
		List<HttpRequest> parsedRequests = new ArrayList<>();
		for(HttpPayload msg : parsedMsgs) {
			if(msg.getMessageType() != HttpMessageType.REQUEST)
				throw new ParseException("Wrong message type="+msg.getMessageType()+" should be="+HttpMessageType.REQUEST);
			HttpRequest req = msg.getHttpRequest();
			if(channel.isSslChannel())
				req.setHttpScheme(HTTPS);

			parsedRequests.add(msg.getHttpRequest());
		}
		return parsedRequests;
	}

	private Memento parse(Memento memento, DataWrapper dataWrapper) {
		Memento resultMemento = parser.parse(memento, dataWrapper);
		
		UnparsedState unParsedState = resultMemento.getUnParsedState();
		switch (unParsedState.getCurrentlyParsing()) {
		case HEADERS:
			if(unParsedState.getCurrentUnparsedSize() > config.maxHeaderSize)
				throw new HttpClientException("Max heaader size="+config.maxHeaderSize+" was exceeded", KnownStatusCode.HTTP_431_REQUEST_HEADERS_TOO_LARGE);
			break;
		case BODY:
		case CHUNK:
			if(unParsedState.getCurrentUnparsedSize() > config.maxBodyOrChunkSize)
				throw new HttpClientException("Body or chunk size limit exceeded", KnownStatusCode.HTTP_413_PAYLOAD_TOO_LARGE);
		default:
			break;
		}
		
		return resultMemento;
	}

	void sendServerException(Channel channel, HttpException exc) {
		listener.incomingError(exc, getHttpServerSocketForChannel(channel));
	}
	
	void farEndClosed(Channel channel) {
		listener.clientClosedChannel(getHttpServerSocketForChannel(channel));
	}

	void applyWriteBackPressure(Channel channel) {
		ResponseSender responseSender = getResponseSenderForChannel(channel);
		listener.applyWriteBackPressure(responseSender);
	}

	void releaseBackPressure(Channel channel) {
		ResponseSender responseSender = getResponseSenderForChannel(channel);
		listener.releaseBackPressure(responseSender);
	}

	private HttpServerSocket getHttpServerSocketForChannel(Channel channel) {
		ChannelSession session = channel.getSession();
		return (HttpServerSocket) session.get("webpieces.httpServerSocket");
	}

	private ResponseSender getResponseSenderForChannel(Channel channel) {
		HttpServerSocket httpServerSocket = getHttpServerSocketForChannel(channel);
		return httpServerSocket.getResponseSender();
	}

}
