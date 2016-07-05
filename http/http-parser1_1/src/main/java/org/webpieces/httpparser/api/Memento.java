package org.webpieces.httpparser.api;

import java.util.List;

import org.webpieces.httpparser.api.dto.HttpPayload;

public interface Memento {

	ParsedStatus getStatus();

	/**
	 * In the case where you pass in bytes 2 or more messages, we
	 * give you back all the parsed messages so far
	 * @return
	 */
	List<HttpPayload> getParsedMessages();
	
	/**
	 * You probably never really need this, but if you need the headers BEFORE the body has finished coming in for
	 * requests with a Content-Length header, then this will be non-null.  You will receive this however with the body
	 * a second time when you call getParsedMessages
	 */
	public HttpPayload getHalfParsedMessage();

	/**
	 * For those who would like to throw an exception if the incoming size of all headers is too large, the current
	 * size can be checked
	 * @return
	 */
	UnparsedState getUnParsedState();
	
}
