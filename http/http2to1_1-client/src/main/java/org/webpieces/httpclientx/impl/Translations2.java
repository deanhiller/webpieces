package org.webpieces.httpclientx.impl;

import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.http2client.api.dto.FullRequest;
import org.webpieces.http2client.api.dto.FullResponse;
import org.webpieces.httpclient11.api.HttpFullRequest;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.*;

public class Translations2 {

	public static HttpFullRequest translate(FullRequest request) {
		//String scheme = request.getHeaders().getScheme();
		String authority = request.getHeaders().getAuthority();
		String path = request.getHeaders().getPath();
		String methodString = request.getHeaders().getMethodString();
		if(!path.startsWith("/"))
			throw new IllegalArgumentException("http2 request :path header must start with /");
		else if(methodString == null)
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
		for(Http2Header header : request.getHeaders().getHeaders()) {
			httpReq.addHeader(new Header(header.getName(), header.getValue()));
		}

		httpReq.addHeader(new Header(KnownHeaderName.HOST, authority));

		HttpFullRequest req = new HttpFullRequest(httpReq, data);
		return req;
	}

	public static FullResponse translate(HttpFullResponse r) {
		FullResponse http2Response = new FullResponse();

		HttpResponse http1Response = r.getResponse();
		for(Header http1Header : http1Response.getHeaders()) {
			http2Response.getHeaders().addHeader(new Http2Header(http1Header.getName(), http1Header.getValue()));
		}

		HttpResponseStatus status = http1Response.getStatusLine().getStatus();
		http2Response.getHeaders().addHeader(new Http2Header(Http2HeaderName.STATUS, ""+status.getKnownStatus().getCode()));

		DataWrapper data = r.getData();
		http2Response.setPayload(data);

		return http2Response;
	}

}
