package org.webpieces.httpclient11.api;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.dto.HttpResponse;

public class HttpFullResponse {

	private HttpResponse response;
	private DataWrapper data;

	public HttpFullResponse(HttpResponse response, DataWrapper data) {
		this.response = response;
		this.data = data;
	}

	public HttpResponse getResponse() {
		return response;
	}

	public void setResponse(HttpResponse response) {
		this.response = response;
	}

	public DataWrapper getData() {
		return data;
	}

	public void setData(DataWrapper data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "HttpFullResponse[response="+response+" body size="+data.getReadableSize()+"]";
	}
}
