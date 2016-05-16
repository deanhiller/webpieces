package org.webpieces.httpproxy.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpproxy.api.HttpRequestListener;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;

import com.webpieces.data.api.DataWrapper;
import com.webpieces.data.api.DataWrapperGenerator;
import com.webpieces.data.api.DataWrapperGeneratorFactory;
import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.Memento;
import com.webpieces.httpparser.api.ParseException;
import com.webpieces.httpparser.api.dto.HttpMessageType;
import com.webpieces.httpparser.api.dto.HttpPayload;
import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.KnownStatusCode;

public class ParserLayer {

	private static final Logger log = LoggerFactory.getLogger(ParserLayer.class);
	
	private static final DataWrapperGenerator generator = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private HttpParser parser;
	private HttpRequestListener listener;
	
	public ParserLayer(HttpParser parser2, HttpRequestListener listener) {
		this.parser = parser2;
		this.listener = listener;
	}

	public void deserialize(Channel channel, ByteBuffer chunk) {
		try {
			List<HttpRequest> parsedRequests = doTheWork(channel, chunk);
		
			for(HttpRequest req : parsedRequests) {
				listener.processHttpRequests(channel, req);
			}
		} catch(ParseException e) {
			//move down to debug level later on..
			//for now, this could actually be we screwed up until we are stable
			log.info("Client screwed up", e);
			listener.sendServerResponse(channel, e, KnownStatusCode.HTTP400);
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

}
