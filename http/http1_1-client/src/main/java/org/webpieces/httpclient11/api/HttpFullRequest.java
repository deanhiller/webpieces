package org.webpieces.httpclient11.api;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.dto.HttpRequest;

public class HttpFullRequest {

	private HttpRequest request;
	private DataWrapper data;
	//TODO: There is another type of request but not sure anyone uses it where you can send trailing headers in the
	//LAST chunk of data BUT you must do 'chunking' to do that rather than Content-Length.  We can add that later if
	//someone needs it
	
	public HttpFullRequest(HttpRequest request, DataWrapper data) {
		this.request = request;
		this.data = data;
	}
	
	public HttpRequest getRequest() {
		return request;
	}
	public DataWrapper getData() {
		return data;
	}

	public void addHeader(Header header) {
		request.addHeader(header);
	}
	
	
}
