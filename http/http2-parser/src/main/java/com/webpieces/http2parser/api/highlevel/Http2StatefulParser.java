package com.webpieces.http2parser.api.highlevel;

import java.util.List;

import org.webpieces.data.api.DataWrapper;

public interface Http2StatefulParser {

	DataWrapper marshal(Http2Payload frame);
	
	List<Http2Payload> parse(DataWrapper newData);

}
