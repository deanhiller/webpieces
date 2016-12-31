package com.webpieces.http2parser.api;

import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.dto.lib.Http2Frame;

public interface Http2Memento {

	Http2ParsedStatus getParsedStatus();
	
	/**
	 * In the case where you pass in bytes 2 or more messages, we
	 * give you back all the parsed messages so far
	 * @return
	 */
	List<Http2Frame> getParsedMessages();
	
	/**
	 * We need a way to reach in to get the leftover data so that the http2 parser can take over from
	 * the http11 parser in the case of an upgrade to http2.
	 */
	DataWrapper getLeftOverData();

}
