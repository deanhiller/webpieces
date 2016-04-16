package com.webpieces.httpparser.api;

import java.util.List;

import com.webpieces.httpparser.api.dto.HttpMessage;

public interface Memento {

	ParsedStatus getStatus();

	/**
	 * In the case where you pass in bytes 2 or more messages, we
	 * give you back all the parsed messages so far
	 * @return
	 */
	List<HttpMessage> getParsedMessages();
	
}
