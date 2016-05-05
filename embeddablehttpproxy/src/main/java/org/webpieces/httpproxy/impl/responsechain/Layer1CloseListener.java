package org.webpieces.httpproxy.impl.responsechain;

import org.webpieces.httpclient.api.CloseListener;
import org.webpieces.httpclient.api.HttpSocket;
import org.webpieces.nio.api.channels.Channel;

public class Layer1CloseListener implements CloseListener {

	private Layer2ResponseListener responseListener;
	private Channel channel;

	public Layer1CloseListener(Layer2ResponseListener responseListener, Channel channel) {
		this.responseListener = responseListener;
		this.channel = channel;
	}

	@Override
	public void farEndClosed(HttpSocket socket) {
		responseListener.farEndClosed(socket, channel);
	}

}
