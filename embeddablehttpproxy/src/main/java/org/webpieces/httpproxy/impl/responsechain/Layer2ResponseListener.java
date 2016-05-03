package org.webpieces.httpproxy.impl.responsechain;

import java.nio.ByteBuffer;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpclient.api.HttpSocket;
import org.webpieces.nio.api.channels.Channel;

import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.dto.HttpPayload;
import com.webpieces.httpparser.api.dto.HttpRequest;

public class Layer2ResponseListener {

	private static final Logger log = LoggerFactory.getLogger(Layer2ResponseListener.class);

	@Inject
	private HttpParser parser;
	
	public void processResponse(Channel channel, HttpRequest req, HttpPayload resp, boolean isComplete) {
		log.info("received response=\n"+resp);

		byte[] respBytes = parser.marshalToBytes(resp);
		ByteBuffer buffer = ByteBuffer.wrap(respBytes);
		channel.write(buffer)
			.thenAccept(p -> wroteBytes(channel))
			.exceptionally(e -> failedWrite(channel, e));
	}

	private Void failedWrite(Channel channel, Throwable e) {
		log.warn("failed to respond to channel="+channel, e);
		return null;
	}

	private void wroteBytes(Channel channel) {
		log.info("wrote bytes out and closing channel="+channel);
		channel.close();
	}

	public Void processError(Channel channel, HttpRequest req, Throwable e) {
		log.warn("could not process req="+req+" from channel="+channel+" due to exception", e);
		
		HttpSocket socket = (HttpSocket) channel.getSession().get("socket");
		
		channel.close();
		socket.closeSocket();
		
		return null;
	}

}
