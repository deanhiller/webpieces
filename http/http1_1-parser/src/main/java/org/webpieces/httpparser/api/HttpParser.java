package org.webpieces.httpparser.api;

import java.nio.ByteBuffer;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.dto.HttpPayload;

public interface HttpParser {

	MarshalState prepareToMarshal();
	
	ByteBuffer marshalToByteBuffer(MarshalState state, HttpPayload request);
	String marshalToString(HttpPayload request);
	
	/**
	 * This must be called for each stream of data you plan to parse
	 * as this contains the state of leftover data still needing to be parsed
	 * when the client does not provide the complete data for one http message
	 * @return
	 */
    Memento prepareToParse();
	
	/**
	 * When dealing with asynchronous I/O, we get 0.5 of an http message or
	 * we get 1.5 or 2.5 of an http message.  This method caches state
	 * allowing the client to just keep feeding data in until the message
	 * is parseable.
	 * 
	 * A special method where you may give part of an HttpMessage or
	 * 1.5 HttpMessages and you can keep feeding the bytes in as you
	 * receive them
	 * 
	 * Call prepareToParse to get the state object to pass back and 
	 * forth.
	 * 
	 */
    Memento parse(Memento state, DataWrapper moreData);

    /**
     * Used when a response is coming for a connect so we don't parse all the data which may
     * be SSL data anyways.  Happens rarely but we hit this scenario
     * 1. We sent connect
     * 2. We sent next request the browser sends before connect is done
     * 3. We get back connect response AND data so we can't use above parse method 
     * 
     */
    Memento parseOnlyHeaders(Memento state, DataWrapper moreData);
	
}
