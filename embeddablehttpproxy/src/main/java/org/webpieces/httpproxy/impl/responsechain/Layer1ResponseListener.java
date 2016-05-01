package org.webpieces.httpproxy.impl.responsechain;

import java.nio.ByteBuffer;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpclient.api.HttpSocket;
import org.webpieces.nio.api.channels.Channel;

import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpResponse;

public class Layer1ResponseListener {

	private static final Logger log = LoggerFactory.getLogger(Layer1ResponseListener.class);

	@Inject
	private HttpParser parser;
	
	public void processResponse(Channel channel, HttpRequest req, HttpResponse resp) {
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

	public Void processError(HttpSocket socket, Channel channel, HttpRequest req, Throwable e) {
		log.warn("could not process req="+req+" from channel="+channel+" due to exception", e);
		
		channel.close();
		socket.closeSocket();
		
		return null;
	}

}
