package org.webpieces.httpproxy.impl.chain;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.httpclient.api.HttpSocket;
import org.webpieces.httpproxy.impl.responsechain.Layer1Response;
import org.webpieces.httpproxy.impl.responsechain.Layer2ResponseListener;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;

import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpResponse;
import com.webpieces.httpparser.api.dto.HttpUri;
import com.webpieces.httpparser.api.dto.UrlInfo;

public class Layer4Processor {

	private static final Logger log = LoggerFactory.getLogger(Layer4Processor.class);
	
	@Inject
	private HttpClient httpClient;
	@Inject
	private Layer2ResponseListener responseListener;
	
	public void processHttpRequests(Channel channel, HttpRequest req) {
		ChannelSession session = channel.getSession();
		HttpSocket socket = (HttpSocket) session.get("socket");
		if(socket == null) {
			socket = openSocketAndSendData(channel, req);
			channel.getSession().put("socket", socket);
		} else {
			sendData(channel, socket, req);
		}
	}

	private void sendData(Channel channel, HttpSocket socket, HttpRequest req) {
		//log.info("sending request(channel="+channel+"(=\n"+req);
		socket.send(req, new Layer1Response(responseListener, channel, req));
	}

	private HttpSocket openSocketAndSendData(Channel channel, HttpRequest req) {
		SocketAddress addr = req.getServerToConnectTo(null);
		
		HttpUri uri = req.getRequestLine().getUri();
		UrlInfo info = uri.getUriBreakdown();
		if(info.getPrefix() != null) { 
			//for sites like http://www.colorado.edu that won't accept full uri path
			//without this, www.colorado.edu was returning 404 ...seems like a bug on their end to be honest
			uri.setUri(info.getFullPath());
		}
		
		HttpSocket socket = httpClient.openHttpSocket(""+channel);
		log.info("connecting to addr="+addr);
		socket.connect(addr)
			  .thenAccept(p->sendData(channel, socket, req))
			  .exceptionally(e -> responseListener.processError(channel, req, e));
		
		return socket;
	}

	public void clientClosedChannel(Channel channel) {
		log.info("closing far end socket. channel="+channel);
		ChannelSession session = channel.getSession();
		HttpSocket socket = (HttpSocket) session.get("socket");
		if(socket != null) {
			socket.closeSocket();
		}
	}

}
