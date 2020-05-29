package org.webpieces.frontend2.impl;

import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.HttpStream;
import org.webpieces.frontend2.api.StreamListener;

public class ProxyStreamListener implements StreamListener {

	private StreamListener httpListener;

	public ProxyStreamListener(StreamListener httpListener) {
		this.httpListener = httpListener;
	}

	@Override
	public HttpStream openStream(FrontendSocket onSocket) {
		HttpStream openStream = httpListener.openStream(onSocket);
		ProxyHttpStream proxyHttpStream = new ProxyHttpStream(openStream);
		return proxyHttpStream;
	}

	@Override
	public void fireIsClosed(FrontendSocket socketThatClosed) {
		httpListener.fireIsClosed(socketThatClosed);
	}

}
