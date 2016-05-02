package com.webpieces.httpparser.api.dto;

import com.webpieces.httpparser.api.common.Header;

public class HttpResponse extends HttpMsg2 {

	private HttpResponseStatusLine statusLine;

	public HttpResponseStatusLine getStatusLine() {
		return statusLine;
	}

	public void setStatusLine(HttpResponseStatusLine statusLine) {
		this.statusLine = statusLine;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((headers == null) ? 0 : headers.hashCode());
		result = prime * result + ((statusLine == null) ? 0 : statusLine.hashCode());
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
		HttpResponse other = (HttpResponse) obj;
		if (headers == null) {
			if (other.headers != null)
				return false;
		} else if (!headers.equals(other.headers))
			return false;
		if (statusLine == null) {
			if (other.statusLine != null)
				return false;
		} else if (!statusLine.equals(other.statusLine))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String response = "" + statusLine;
		for(Header header : headers) {
			response += header;
		}
		//The final \r\n at the end of the message
		return response + "\r\n";
	}

	@Override
	public HttpMessageType getMessageType() {
		return HttpMessageType.RESPONSE;
	}
	
}
