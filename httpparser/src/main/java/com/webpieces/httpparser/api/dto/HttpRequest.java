package com.webpieces.httpparser.api.dto;

import com.webpieces.httpparser.api.DataWrapper;
import com.webpieces.httpparser.api.common.Header;

public class HttpRequest extends HttpMessage {

	private HttpRequestLine requestLine;
	
	public HttpRequestLine getRequestLine() {
		return requestLine;
	}

	public void setRequestLine(HttpRequestLine requestLine) {
		this.requestLine = requestLine;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((headers == null) ? 0 : headers.hashCode());
		result = prime * result + ((requestLine == null) ? 0 : requestLine.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HttpRequest other = (HttpRequest) obj;
		if (headers == null) {
			if (other.headers != null)
				return false;
		} else if (!headers.equals(other.headers))
			return false;
		if (requestLine == null) {
			if (other.requestLine != null)
				return false;
		} else if (!requestLine.equals(other.requestLine))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String request = "" + requestLine;
		for(Header header : headers) {
			request += header;
		}
		//The final \r\n at the end of the message
		return request + "\r\n";
	}

	@Override
	public HttpMessageType getMessageType() {
		return HttpMessageType.REQUEST;
	}

}
