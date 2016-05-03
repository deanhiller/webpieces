package com.webpieces.httpparser.api.dto;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.webpieces.httpparser.api.common.Header;
import com.webpieces.httpparser.api.common.KnownHeaderName;

public class HttpRequest extends HttpMsg2 {

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

	/**
	 * Return the SocketAddress from the information in the http request.  Sometimes there is not
	 * enough information in which case one must provide the port number.  The host comes from
	 * the absolute uri first and if host is not there, it comes from the HOST header next and if
	 * not found there, this method will throw an exception.  port parameter is only required
	 * when there is not an absolute uri. (ie. only HOST header exists)
	 * 
	 * @param request
	 * @param port
	 * @return
	 */
	public SocketAddress getServerToConnectTo(HttpRequest request, Integer port) {
		UrlInfo urlInfo = request.getRequestLine().getUri().getHostPortAndType();
		
		String host = urlInfo.getHost();
		if(host == null) {
			Header hostHeader = getHeaderLookupStruct().getLastInstanceOfHeader(KnownHeaderName.HOST);
			if(hostHeader == null)
				throw new IllegalStateException("There is no host in url nor in HOST header to be found in this request");
			host = hostHeader.getValue();
		}
		
		Integer resolvedPort = urlInfo.getResolvedPort();
		if(resolvedPort == null) {
			if(port == null)
				throw new IllegalArgumentException("The port is required since there is no port information in the HttpRequest");
			resolvedPort = port;
		}

		return new InetSocketAddress(host, resolvedPort);
	}
	
}
