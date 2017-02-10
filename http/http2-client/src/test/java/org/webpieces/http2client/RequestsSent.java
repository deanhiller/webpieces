package org.webpieces.http2client;

public class RequestsSent {

	private RequestHolder request1;
	private RequestHolder request2;

	public RequestsSent(RequestHolder request1, RequestHolder request2) {
		this.request1 = request1;
		this.request2 = request2;
	}

	public RequestHolder getRequest1() {
		return request1;
	}

	public RequestHolder getRequest2() {
		return request2;
	}
	
}
