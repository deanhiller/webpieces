package org.webpieces.httpproxy.impl.chain;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataChunk;
import org.webpieces.nio.api.libs.ChannelSession;

import com.webpieces.httpparser.api.DataWrapper;
import com.webpieces.httpparser.api.DataWrapperGenerator;
import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.Memento;
import com.webpieces.httpparser.api.ParseException;
import com.webpieces.httpparser.api.ParsedStatus;
import com.webpieces.httpparser.api.dto.HttpMessage;
import com.webpieces.httpparser.api.dto.HttpMessageType;
import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpStatusType;
import com.webpieces.httpparser.api.dto.KnownStatusCode;

public class Layer3Parser {

	private static final Logger log = LoggerFactory.getLogger(LayerZSendBadResponse.class);
	
	@Inject
	private HttpParser parser;
	@Inject
	private DataWrapperGenerator generator;
	@Inject
	private Layer4Processor processor;
	@Inject
	private LayerZSendBadResponse badResponse;
	
	public void deserialize(Channel channel, DataChunk chunk) {
		try {
			List<HttpRequest> parsedRequests = doTheWork(channel, chunk);
		
			processor.processHttpRequests(channel, parsedRequests);
		} catch(ParseException e) {
			//move down to debug level later on..
			//for now, this could actually be we screwed up until we are stable
			log.info("Client screwed up", e);
			badResponse.sendServerResponse(channel, e, KnownStatusCode.HTTP400);
		}
	}

	private List<HttpRequest> doTheWork(Channel channel, DataChunk chunk) {
		ChannelSession session = channel.getSession();		
		Memento memento = (Memento) session.get("memento");
		
		if(memento == null) {
			memento = parser.prepareToParse();
			session.put("memento", memento);
		}

		ByteBuffer data = chunk.getData();
		DataWrapper dataWrapper = generator.wrapByteBuffer(data);
		
		Memento resultMemento = parse(memento, dataWrapper);

//		if(resultMemento.getStatus() == ParsedStatus.NEED_MORE_DATA) {
//			return;
//		}
		
		List<HttpMessage> parsedMsgs = resultMemento.getParsedMessages();
		List<HttpRequest> parsedRequests = new ArrayList<>();
		for(HttpMessage msg : parsedMsgs) {
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
