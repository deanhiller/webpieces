package org.webpieces.httpproxy.impl.responsechain;

import org.webpieces.httpclient.api.ResponseListener;
import org.webpieces.nio.api.channels.Channel;

import com.webpieces.httpparser.api.dto.HttpChunk;
import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpResponse;

public class Layer1Response implements ResponseListener {

	private Layer2ResponseListener responseListener;
	private Channel channel;
	private HttpRequest req;

	public Layer1Response(Layer2ResponseListener responseListener, Channel channel, HttpRequest req) {
		this.responseListener = responseListener;
		this.channel = channel;
		this.req = req;
	}

	@Override
	public void incomingResponse(HttpResponse resp, boolean isComplete) {
		responseListener.processResponse(channel, req, resp, isComplete);
	}

	@Override
	public void incomingChunk(HttpChunk chunk, boolean isLastChunk) {
		responseListener.processResponse(channel, req, chunk, isLastChunk);
	}

	@Override
	public void failure(Throwable e) {
		responseListener.processError(channel, req, e);
	}

}
