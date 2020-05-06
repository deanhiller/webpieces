package org.webpieces.httpclientx.impl;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2parser.api.dto.Http2Method;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.http2client.api.dto.FullRequest;
import org.webpieces.http2client.api.dto.FullResponse;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.*;

public class Translations2 {

	private static final Logger log = LoggerFactory.getLogger(Translations2.class);

	public static HttpFullRequest translate(FullRequest request) {
		//String scheme = request.getHeaders().getScheme();
		Http2Request headers = request.getHeaders();
		String authority = headers.getAuthority();
		String path = headers.getPath();
		String methodString = headers.getMethodString();
		if(methodString == null)
			throw new IllegalArgumentException("http2 :method header is required");
		else if(authority == null) {
			throw new IllegalArgumentException("http1 required host header so http2 message must have :authority header set");
		}

		HttpRequestLine reqLine = new HttpRequestLine();
		reqLine.setUri(new HttpUri(path));
		reqLine.setMethod(new HttpRequestMethod(methodString));
		reqLine.setVersion(new HttpVersion());

		HttpRequest httpReq = new HttpRequest();
		httpReq.setRequestLine(reqLine);
		DataWrapper data = request.getPayload();

		//translate all other headers here as well...
		for(Http2Header header : headers.getHeaders()) {
			if(!header.getName().startsWith(":")) //All standard headers go elsewhere except HOST which we do below
				httpReq.addHeader(new Header(header.getName(), header.getValue()));
		}

		httpReq.addHeader(new Header(KnownHeaderName.HOST, authority));

		HttpFullRequest req = new HttpFullRequest(httpReq, data);
		return req;
	}

	public static FullResponse translate(HttpFullResponse r) {
		Http2Response headers = new Http2Response();
		HttpResponse http1Response = r.getResponse();
		for(Header http1Header : http1Response.getHeaders()) {
			headers.addHeader(new Http2Header(http1Header.getName(), http1Header.getValue()));
		}

		HttpResponseStatus status = http1Response.getStatusLine().getStatus();

		headers.addHeader(new Http2Header(Http2HeaderName.STATUS, ""+status.getCode()));

		DataWrapper data = r.getData();

		FullResponse http2Response = new FullResponse(headers, data, null);

		return http2Response;
	}

}
