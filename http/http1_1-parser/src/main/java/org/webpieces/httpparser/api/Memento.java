package org.webpieces.httpparser.api;

import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.dto.HttpPayload;

public interface Memento {

	/**
	 * In the case where you pass in bytes 2 or more messages, we
	 * give you back all the parsed messages so far
	 * @return
	 */
	List<HttpPayload> getParsedMessages();

	/**
	 * For those who would like to throw an exception if the incoming size of all headers is too large, the current
	 * size can be checked
	 * @return
	 */
	UnparsedState getUnParsedState();

	/**
	 * We need a way to reach in to get the leftover data so that the http2 parser can take over from
	 * the http11 parser in the case of an upgrade to http2.
	 */
	DataWrapper getLeftOverData();
	
}
