package com.webpieces.http2parser.api;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.dto.Http2Frame;

public interface Http2Parser2 {
	
	//taking a stab at my guess of what we could do instead
	Http2Memento prepareToParse(); 
	
	Http2Memento parse(Http2Memento memento, DataWrapper newData);

	DataWrapper marshal(Http2Frame frame);
}
