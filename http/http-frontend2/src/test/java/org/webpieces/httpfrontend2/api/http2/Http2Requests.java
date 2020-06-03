package org.webpieces.httpfrontend2.api.http2;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import com.webpieces.http2.api.dto.highlevel.Http2Push;
import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.highlevel.Http2Trailers;
import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import com.webpieces.http2engine.impl.shared.data.HeaderSettings;

public class Http2Requests {
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

	public static Http2Request createRequest(int streamId, boolean eos) {
		List<Http2Header> headers = new ArrayList<>();
		
	    headers.add(new Http2Header(Http2HeaderName.METHOD, "GET"));
	    headers.add(new Http2Header(Http2HeaderName.AUTHORITY, "somehost.com"));
	    headers.add(new Http2Header(Http2HeaderName.PATH, "/"));
	    headers.add(new Http2Header(Http2HeaderName.SCHEME, "http"));
	    headers.add(new Http2Header(Http2HeaderName.ACCEPT, "*/*"));
	    headers.add(new Http2Header(Http2HeaderName.ACCEPT_ENCODING, "gzip, deflate"));
	    headers.add(new Http2Header(Http2HeaderName.USER_AGENT, "webpieces/1.15.0"));
	    
	    Http2Request request = new Http2Request(headers);
	    request.setStreamId(streamId);
	    request.setEndOfStream(eos);
		return request;
	}

	public static Http2Response createResponse(int streamId) {
		return createResponse(streamId, true);
	}
	
	public static Http2Response createResponse(int streamId, boolean eos) {
		List<Http2Header> headers = new ArrayList<>();
	    headers.add(new Http2Header(Http2HeaderName.SERVER, "id"));
	    headers.add(new Http2Header(Http2HeaderName.STATUS, "200"));
	    
	    Http2Response response = new Http2Response(headers);
	    response.setEndOfStream(eos);
	    response.setStreamId(streamId);
	    
		return response;
	}

	public static DataFrame createData1(int streamId, boolean eos) {
		DataFrame data = new DataFrame(streamId, eos);
		data.setData(DATA_GEN.wrapByteArray(new byte[] { 3, 4, 4 }));
		return data;
	}

	public static DataFrame createData2(int streamId, boolean eos) {
		DataFrame data = new DataFrame(streamId, eos);
		data.setData(DATA_GEN.wrapByteArray(new byte[] { 3 }));
		return data;
	}
	
	public static Http2Trailers createTrailers(int streamId) {
		List<Http2Header> headers = new ArrayList<>();
	    headers.add(new Http2Header(Http2HeaderName.SERVER, "trailing"));
	    
	    Http2Trailers trailers = new Http2Trailers(headers);
	    trailers.setEndOfStream(true);
	    trailers.setStreamId(streamId);
		return trailers;
	}
	
	public static Http2Push createPush(int streamId) {
		Http2Push push = new Http2Push();
		push.setStreamId(streamId);
	    push.addHeader(new Http2Header(Http2HeaderName.SERVER, "me"));

		return push;
	}
}
