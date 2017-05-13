package org.webpieces.http2client.util;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.http2client.api.dto.Http2Request;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.impl.shared.HeaderSettings;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

public class Requests {
	protected static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	public static HeaderSettings createSomeSettings() {
		HeaderSettings settings = new HeaderSettings();
		settings.setHeaderTableSize(4099);
		settings.setInitialWindowSize(5009);
		settings.setMaxConcurrentStreams(1L);
		settings.setMaxFrameSize(16385);
		settings.setMaxHeaderListSize(5222);
		settings.setPushEnabled(true);
		return settings;
	}

	public static Http2Headers createRequest() {
		return createRequest(true);
	}
	
	static Http2Headers createRequest(boolean eos) {
		List<Http2Header> headers = new ArrayList<>();
		
	    headers.add(new Http2Header(Http2HeaderName.METHOD, "GET"));
	    headers.add(new Http2Header(Http2HeaderName.AUTHORITY, "somehost.com"));
	    headers.add(new Http2Header(Http2HeaderName.PATH, "/"));
	    headers.add(new Http2Header(Http2HeaderName.SCHEME, "http"));
	    headers.add(new Http2Header(Http2HeaderName.HOST, "somehost.com"));
	    headers.add(new Http2Header(Http2HeaderName.ACCEPT, "*/*"));
	    headers.add(new Http2Header(Http2HeaderName.ACCEPT_ENCODING, "gzip, deflate"));
	    headers.add(new Http2Header(Http2HeaderName.USER_AGENT, "webpieces/1.15.0"));
	    
	    Http2Headers request = new Http2Headers(headers);
	    request.setEndOfStream(eos);
		return request;
	}

	public static Http2Headers createResponse(int streamId) {
		List<Http2Header> headers = new ArrayList<>();
	    headers.add(new Http2Header(Http2HeaderName.SERVER, "me"));
	    
	    Http2Headers response = new Http2Headers(headers);
	    response.setEndOfStream(false);
	    
	    response.setStreamId(streamId);
	    
		return response;
	}

	public static Http2Headers createEosResponse(int streamId) {
		List<Http2Header> headers = new ArrayList<>();
	    headers.add(new Http2Header(Http2HeaderName.SERVER, "me"));
	    
	    Http2Headers response = new Http2Headers(headers);
	    response.setEndOfStream(true);
	    
	    response.setStreamId(streamId);
	    
		return response;
	}
	
	public static Http2Push createPush(int streamId) {
		Http2Push push = new Http2Push();
		push.setStreamId(streamId);
		push.setPromisedStreamId(2);
	    push.addHeader(new Http2Header(Http2HeaderName.SERVER, "me"));

		return push;
	}

	public static RstStreamFrame createReset(int streamId) {
		return new RstStreamFrame(streamId, Http2ErrorCode.CANCEL);
	}

	public static Http2Request createHttp2Request() {
		Http2Request req = new Http2Request();
		req.setHeaders(createRequest());
		req.setPayload(dataGen.wrapByteArray(new byte[] { 3, 4 }));
		return req;
	}

	public static DataFrame createData(int streamId) {
		DataFrame data = new DataFrame(streamId, true);
		DataWrapper wrapByteArray = dataGen.wrapByteArray(new byte[] {2, 3});
		data.setData(wrapByteArray);
		return data;
	}

}
