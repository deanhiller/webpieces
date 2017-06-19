package org.webpieces.httpclient11.api;

import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpRequest;

public class HttpFullRequest {

	private HttpRequest request;
	private HttpData data;
	//TODO: There is another type of request but not sure anyone uses it where you can send trailing headers in the
	//LAST chunk of data BUT you must do 'chunking' to do that rather than Content-Length
	
	public HttpRequest getRequest() {
		return request;
	}
	public HttpData getData() {
		return data;
	}
	
	
}
