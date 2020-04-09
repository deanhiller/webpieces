package org.webpieces.http2client.util;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.http2client.api.dto.FullRequest;

import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.hpack.api.dto.Http2Trailers;
import com.webpieces.http2engine.impl.shared.data.HeaderSettings;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.StatusCode;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

public class Requests {
	protected static final DataWrapperGenerator DATA_GEN = DataWrapperGeneratorFactory.createDataWrapperGenerator();

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

	public static Http2Request createRequest() {
		return createRequest(true);
	}
	
	static Http2Request createRequest(boolean eos) {
		List<Http2Header> headers = new ArrayList<>();
		
	    headers.add(new Http2Header(Http2HeaderName.METHOD, "GET"));
	    headers.add(new Http2Header(Http2HeaderName.AUTHORITY, "somehost.com"));
	    headers.add(new Http2Header(Http2HeaderName.PATH, "/"));
	    headers.add(new Http2Header(Http2HeaderName.SCHEME, "http"));
	    headers.add(new Http2Header(Http2HeaderName.ACCEPT, "*/*"));
	    headers.add(new Http2Header(Http2HeaderName.ACCEPT_ENCODING, "gzip, deflate"));
	    headers.add(new Http2Header(Http2HeaderName.USER_AGENT, "webpieces/1.15.0"));
	    
	    Http2Request request = new Http2Request(headers);
	    request.setEndOfStream(eos);
		return request;
	}

	public static Http2Trailers createTrailers() {
		List<Http2Header> headers = new ArrayList<>();
		
	    headers.add(new Http2Header(Http2HeaderName.ACCEPT, "*/*"));
	    headers.add(new Http2Header(Http2HeaderName.ACCEPT_ENCODING, "gzip, deflate"));
	    headers.add(new Http2Header(Http2HeaderName.USER_AGENT, "webpieces/1.15.0"));
	    
	    Http2Trailers trailers = new Http2Trailers(headers);
		return trailers;
	}
	
	public static Http2Response createResponse(int streamId) {
		List<Http2Header> headers = new ArrayList<>();
		headers.add(new Http2Header(Http2HeaderName.STATUS, StatusCode.HTTP_200_OK.getCodeString()));
	    headers.add(new Http2Header(Http2HeaderName.SERVER, "me"));
	    
	    Http2Response response = new Http2Response(headers);
	    response.setEndOfStream(false);
	    
	    response.setStreamId(streamId);
	    
		return response;
	}

	public static Http2Response createEosResponse(int streamId) {
		List<Http2Header> headers = new ArrayList<>();
		headers.add(new Http2Header(Http2HeaderName.STATUS, StatusCode.HTTP_200_OK.getCodeString()));
	    headers.add(new Http2Header(Http2HeaderName.SERVER, "me"));
	    
	    Http2Response response = new Http2Response(headers);
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

	public static FullRequest createHttp2Request() {
		FullRequest req = new FullRequest();
		req.setHeaders(createRequest());
		req.setPayload(DATA_GEN.wrapByteArray(new byte[] { 3, 4 }));
		return req;
	}

	public static DataFrame createData(int streamId, boolean eos) {
		DataFrame data = new DataFrame(streamId, eos);
		DataWrapper wrapByteArray = DATA_GEN.wrapByteArray(new byte[] {2, 3});
		data.setData(wrapByteArray);
		return data;
	}

	public static DataFrame createBigData(int streamId, boolean eos) {
		DataFrame data = new DataFrame(streamId, eos);
		String s = "hi there, this is a bit more data so that we can test a few things out";
		byte[] bytes = s.getBytes();
		DataWrapper wrapByteArray = DATA_GEN.wrapByteArray(bytes);
		data.setData(wrapByteArray);
		return data;
	}
	
}
