package org.webpieces.httpparser.api;

import java.nio.ByteBuffer;
import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.dto.HttpPayload;

public interface HttpStatefulParser {

	ByteBuffer marshalToByteBuffer(HttpPayload request);
	String marshalToString(HttpPayload request);
	
	List<HttpPayload> parse(DataWrapper moreData);

}
