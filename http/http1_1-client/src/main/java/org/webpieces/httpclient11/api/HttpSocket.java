package org.webpieces.httpclient11.api;

import java.net.InetSocketAddress;

import org.webpieces.nio.api.channels.HostWithPort;
import org.webpieces.util.futures.XFuture;

import org.webpieces.httpparser.api.dto.HttpRequest;

public interface HttpSocket {

	public XFuture<Void> connect(HostWithPort addr);

	/**
	 * @deprecated
	 */
	@Deprecated
	public XFuture<Void> connect(InetSocketAddress addr);

	/**
	 * This can be used ONLY if 'you' know that the far end does NOT sended a chunked download. 
	 * The reason is in a chunked download, we don't want to blow up your RAM.  Some apis like
	 * twitters streaming api and we would never ever be done and have a full response.  Others
	 * are just a very very large download you don't want existing in RAM anyways.
	 * 
	 * @param request
	 */
	//TODO: Implement timeout for clients so that requests will timeout
	public XFuture<HttpFullResponse> send(HttpFullRequest request);

	public HttpStreamRef send(HttpRequest request, HttpResponseListener l);

	public XFuture<Void> close();

	public boolean isClosed();
}
