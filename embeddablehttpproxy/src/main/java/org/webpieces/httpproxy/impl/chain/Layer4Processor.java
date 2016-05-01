package org.webpieces.httpproxy.impl.chain;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.httpclient.api.HttpSocket;
import org.webpieces.httpproxy.impl.responsechain.Layer1ResponseListener;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;

import com.webpieces.httpparser.api.common.Header;
import com.webpieces.httpparser.api.common.KnownHeaderName;
import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpResponse;
import com.webpieces.httpparser.api.dto.UrlInfo;

public class Layer4Processor {

	private static final Logger log = LoggerFactory.getLogger(Layer4Processor.class);
	
	@Inject
	private HttpClient httpClient;
	@Inject
	private Layer1ResponseListener responseListener;
	
	public void processHttpRequests(Channel channel, HttpRequest req) {
		ChannelSession session = channel.getSession();
		if(session.get("socket") != null)
			throw new UnsupportedOperationException("not supported yet");
		
		HttpSocket socket = httpClient.openHttpSocket(""+channel);
		channel.getSession().put("socket", socket);
		
		UrlInfo urlInfo = req.getRequestLine().getUri().getHostPortAndType();
		
		String host = urlInfo.getHost();
		int port = urlInfo.getResolvedPort();
		
		//override for right now...
		req.getRequestLine().getUri().setUri("/");
		
		Header header = req.getHeaderLookupStruct().getHeader(KnownHeaderName.HOST);
		String value = header.getValue();

		SocketAddress addr = new InetSocketAddress(value, port);
		log.info("connecting to addr="+addr);
		socket.connect(addr)
			  .thenCompose(p->send(socket, req))
			  .thenAccept(resp -> responseListener.processResponse(channel, req, resp))
			  .exceptionally(e -> responseListener.processError(socket, channel, req, e));
	}

	private CompletableFuture<HttpResponse> send(HttpSocket socket, HttpRequest req) {
		log.info("sending request=\n"+req);
		return socket.send(req);
	}

	public void farEndClosed(Channel channel) {
		log.info("closing far end socket. channel="+channel);
		ChannelSession session = channel.getSession();
		HttpSocket socket = (HttpSocket) session.get("socket");
		if(socket != null) {
			socket.closeSocket();
		}
	}

}
