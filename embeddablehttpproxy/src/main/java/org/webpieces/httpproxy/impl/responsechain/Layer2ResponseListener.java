package org.webpieces.httpproxy.impl.responsechain;

import java.nio.ByteBuffer;
import java.nio.channels.UnresolvedAddressException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpclient.api.HttpSocket;
import org.webpieces.httpproxy.impl.chain.LayerZSendBadResponse;
import org.webpieces.nio.api.channels.Channel;

import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.dto.HttpPayload;
import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.KnownStatusCode;

public class Layer2ResponseListener {

	private static final Logger log = LoggerFactory.getLogger(Layer2ResponseListener.class);

	@Inject
	private HttpParser parser;
	@Inject
	private LayerZSendBadResponse badResponse;
	
	public void processResponse(Channel channel, HttpRequest req, HttpPayload resp, boolean isComplete) {
		log.info("received response(channel="+channel+").  type="+resp.getClass().getSimpleName()+" complete="+isComplete+" resp=\n"+resp);

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
		log.info("wrote bytes out channel="+channel);
	}

	public Void processError(Channel channel, HttpRequest req, Throwable e) {
		log.warn("could not process req="+req+" from channel="+channel+" due to exception", e);

		if(e.getCause() instanceof UnresolvedAddressException) {
			badResponse.sendServerResponse(channel, e, KnownStatusCode.HTTP404);
		} else {
			badResponse.sendServerResponse(channel, e, KnownStatusCode.HTTP500);
		}
		channel.close();
		
		return null;
	}

	public void farEndClosed(HttpSocket socket, Channel channel) {
		//since socket is closing, close the channel from the browser...
		log.info("closing connection from browser.  channel="+channel);
		channel.close();
	}

}
