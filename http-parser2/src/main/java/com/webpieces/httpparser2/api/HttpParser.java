package com.webpieces.httpparser2.api;

import java.nio.ByteBuffer;

import com.webpieces.data.api.DataWrapper;
import com.webpieces.httpparser2.api.dto.Http2Frame;

public interface HttpParser {

	public ByteBuffer marshalToByteBuffer(Http2Frame request);
	
	//TODO: This needs to change!!!  We need to pass in a data structure that 
	//we can write to so it could be a byte[] or it could be a ByteBuffer
	//or anything else but we also need to know the size ahead of time or?
	public byte[] marshalToBytes(Http2Frame request);
	
	public String marshalToString(Http2Frame request);
	
	/**
	 * This must be called for each stream of data you plan to parse
	 * as this contains the state of leftover data still needing to be parsed
	 * when the client does not provide the complete data for one http message
	 * @return
	 */
	public Memento prepareToParse();
	
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
	 * @param msg
	 * @return
	 */	
	public Memento parse(Memento state, DataWrapper moreData);
	
	/**
	 * When you know you have the complete http message, then you can simply
	 * use this method
	 * 
	 * @param msg
	 * @return
	 */
	//TODO: convert api to DataWrapper?
	public Http2Frame unmarshal(byte[] msg);
}
