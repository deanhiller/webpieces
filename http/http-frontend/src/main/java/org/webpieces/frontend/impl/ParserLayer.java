package org.webpieces.frontend.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend.api.FrontendSocket;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.httpparser.api.ParseException;
import org.webpieces.httpparser.api.dto.HttpMessageType;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;

public class ParserLayer {

	private static final Logger log = LoggerFactory.getLogger(ParserLayer.class);
	
	private static final DataWrapperGenerator generator = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private HttpParser parser;
	private TimedListener listener;
	private boolean isHttps;
	
	public ParserLayer(HttpParser parser2, TimedListener listener, boolean isHttps) {
		this.parser = parser2;
		this.listener = listener;
		this.isHttps = isHttps;
	}

	public void deserialize(Channel channel, ByteBuffer chunk) {
		try {
			List<HttpRequest> parsedRequests = doTheWork(channel, chunk);
		
			for(HttpRequest req : parsedRequests) {
				listener.processHttpRequests(translate(channel), req, isHttps);
			}
		} catch(ParseException e) {
			//move down to debug level later on..
			log.info("Client screwed up", e);
			listener.sendServerResponse(translate(channel), e, KnownStatusCode.HTTP400);
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

//		if(resultMemento.getStatus() == ParsedStatus.NEED_MORE_DATA) {
//			return;
//		}
		
		List<HttpPayload> parsedMsgs = resultMemento.getParsedMessages();
		List<HttpRequest> parsedRequests = new ArrayList<>();
		for(HttpPayload msg : parsedMsgs) {
			if(msg.getMessageType() != HttpMessageType.REQUEST)
				throw new ParseException("Wrong message type="+msg.getMessageType()+" should be="+HttpMessageType.REQUEST);
			parsedRequests.add(msg.getHttpRequest());
		}
		return parsedRequests;
	}

	private Memento parse(Memento memento, DataWrapper dataWrapper) {
		try {
			Memento resultMemento = parser.parse(memento, dataWrapper);
			return resultMemento;
		} catch(Throwable e) {
			throw new ParseException("Parser could not parse", e);
		}
	}

	public void sendServerResponse(Channel channel, Throwable exc, KnownStatusCode status) {
		listener.sendServerResponse(translate(channel), exc, status);
	}

	public void openedConnection(Channel channel, boolean isReadyForWrites) {
		FrontendSocket socket = translate(channel);
		listener.clientOpenChannel(socket, isReadyForWrites);
	}
	
	public void farEndClosed(Channel channel) {
		listener.clientClosedChannel(translate(channel));		
	}

	public void applyWriteBackPressure(Channel channel) {
		listener.applyWriteBackPressure(translate(channel));
	}

	public void releaseBackPressure(Channel channel) {
		listener.releaseBackPressure(translate(channel));
	}

	private FrontendSocket translate(Channel channel) {
		ChannelSession session = channel.getSession();
		FrontendSocketImpl socket = (FrontendSocketImpl) session.get("webpieces.frontendSocket");
		if(socket == null) {
			socket = new FrontendSocketImpl(channel, parser);
			session.put("webpieces.frontendSocket", socket);
		}
		return socket;
	}

}
