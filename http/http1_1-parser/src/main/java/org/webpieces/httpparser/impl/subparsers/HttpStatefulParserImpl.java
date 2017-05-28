package org.webpieces.httpparser.impl.subparsers;

import java.nio.ByteBuffer;
import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpStatefulParser;
import org.webpieces.httpparser.api.MarshalState;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.httpparser.api.dto.HttpPayload;

public class HttpStatefulParserImpl implements HttpStatefulParser {

	private HttpParser parser;
	private Memento memento;
	private MarshalState state;

	public HttpStatefulParserImpl(HttpParser statelessParser) {
		this.parser = statelessParser;
		memento = parser.prepareToParse();
		state = parser.prepareToMarshal();
	}

	@Override
	public ByteBuffer marshalToByteBuffer(HttpPayload request) {
		return parser.marshalToByteBuffer(state, request);
	}

	@Override
	public List<HttpPayload> parse(DataWrapper moreData) {
		memento = parser.parse(memento, moreData);
		return memento.getParsedMessages();
	}

}
