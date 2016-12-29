package com.webpieces.http2parser.api.highlevel;

import org.webpieces.data.api.DataWrapper;

public interface Http2StatefulParser {

	void marshal(Http2Payload frame);
	
	void parse(DataWrapper newData);

}
