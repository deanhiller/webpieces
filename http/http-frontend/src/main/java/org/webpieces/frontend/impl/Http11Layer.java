package org.webpieces.frontend.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpServerSocket;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.httpcommon.api.exceptions.HttpClientException;
import org.webpieces.httpcommon.api.exceptions.HttpException;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.httpparser.api.ParseException;
import org.webpieces.httpparser.api.UnparsedState;
import org.webpieces.httpparser.api.dto.HttpMessageType;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;

import static org.webpieces.httpparser.api.dto.HttpRequest.HttpScheme.HTTPS;

public class Http11Layer {

	private static final DataWrapperGenerator generator = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private HttpParser parser;
	private TimedListener listener;
	private boolean isHttps;
	private FrontendConfig config;
	
	Http11Layer(HttpParser parser2, TimedListener listener, FrontendConfig config, boolean isHttps) {
		this.parser = parser2;
		this.listener = listener;
		this.config = config;
		this.isHttps = isHttps;
	}

	void deserialize(Channel channel, ByteBuffer chunk) {
		List<HttpRequest> parsedRequests = doTheWork(channel, chunk);

        // TODO: if we get chunks, send these to incomingData.
		for(HttpRequest req : parsedRequests) {
			listener.incomingRequest(req, new RequestId(0), !req.isHasChunkedTransferHeader(), getResponseSenderForChannel(channel));
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
