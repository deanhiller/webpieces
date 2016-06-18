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
	
}
