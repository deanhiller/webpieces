package org.webpieces.httpproxy.impl.chain;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpclient.api.CloseListener;
import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.httpclient.api.HttpSocket;
import org.webpieces.httpproxy.api.ProxyConfig;
import org.webpieces.httpproxy.impl.responsechain.Layer1Response;
import org.webpieces.httpproxy.impl.responsechain.Layer2ResponseListener;
import org.webpieces.nio.api.channels.Channel;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpUri;
import com.webpieces.httpparser.api.dto.UrlInfo;

public class Layer4Processor {

	private static final Logger log = LoggerFactory.getLogger(Layer4Processor.class);

	@Inject
	private ProxyConfig config;
	@Inject
	private HttpClient httpClient;
	@Inject
	private Layer2ResponseListener layer2Processor;
	private final Cache<SocketAddress, HttpSocket> cache;
	
	public Layer4Processor() {
		cache = CacheBuilder.newBuilder()
			    .concurrencyLevel(4)
			    .maximumSize(10000)
			    .expireAfterAccess(3, TimeUnit.MINUTES)
			    .removalListener(new SocketExpiredListener())
			    .build();
	}
	
	public void processHttpRequests(Channel channel, HttpRequest req) {
		log.info("incoming request. channel="+channel+"=\n"+req);
		InetSocketAddress addr = req.getServerToConnectTo(null);
		if(config.isForceAllConnectionToHttps()) {
			addr = new InetSocketAddress(addr.getHostName(), 443);
		}

		HttpUri uri = req.getRequestLine().getUri();
		UrlInfo info = uri.getUriBreakdown();
		if(info.getPrefix() != null) { 
			//for sites like http://www.colorado.edu that won't accept full uri path
			//without this, www.colorado.edu was returning 404 ...seems like a bug on their end to be honest
			uri.setUri(info.getFullPath());
		}
		
		HttpSocket socket = cache.getIfPresent(addr);
		if(socket != null) {
			sendData(channel, socket, req);
		} else {
			openAndConnectSocket(addr, req, channel);
		}
	}

	private void sendData(Channel channel, HttpSocket socket, HttpRequest req) {
		socket.send(req, new Layer1Response(layer2Processor, channel, req));
	}

	private HttpSocket openAndConnectSocket(InetSocketAddress addr, HttpRequest req, Channel channel) {
		HttpSocket socket = httpClient.openHttpSocket(""+addr.getHostName()+"-"+addr.getPort(), new Layer1CloseListener(addr));
		log.info("connecting to addr="+addr);
		socket.connect(addr)
				.thenAccept(s -> {
					sendData(channel, socket, req);
					cache.put(addr, socket);
				})
				.exceptionally(e -> layer2Processor.processError(channel, req, e));
		
		return socket;
	}

	public void clientClosedChannel(Channel channel) {
		log.info("browser client closed channel="+channel);
	}

	private class SocketExpiredListener implements RemovalListener<SocketAddress, HttpSocket> {
		@Override
		public void onRemoval(RemovalNotification<SocketAddress, HttpSocket> notification) {
			log.info("closing socket="+notification.getKey()+".  cache removal cause="+notification.getCause());
			HttpSocket socket = notification.getValue();
			socket.closeSocket();
		}
	}
	
	private class Layer1CloseListener implements CloseListener {
		private SocketAddress addr;

		public Layer1CloseListener(SocketAddress addr) {
			this.addr = addr;
		}

		@Override
		public void farEndClosed(HttpSocket socket) {
			log.info("socket addr="+addr+" closed, invalidating cache");
			cache.invalidate(addr);
		}
	}
}
