package org.webpieces.httpproxy.impl.chain;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.frontend.api.HttpServerSocket;
import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.httpclient.api.HttpClientSocket;
import org.webpieces.httpcommon.api.CloseListener;
import org.webpieces.httpcommon.api.HttpSocket;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.httpcommon.api.RequestSender;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.httpcommon.api.exceptions.HttpException;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpUri;
import org.webpieces.httpparser.api.dto.UrlInfo;
import org.webpieces.httpproxy.api.ProxyConfig;
import org.webpieces.httpproxy.impl.responsechain.Layer1Response;
import org.webpieces.httpproxy.impl.responsechain.Layer2ResponseListener;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public class Layer4Processor implements RequestListener {

	private static final Logger log = LoggerFactory.getLogger(Layer4Processor.class);

	@Inject
	private ProxyConfig config;
	@Inject
	private HttpClient httpClient;
	@Inject
	private Layer2ResponseListener layer2Processor;
	@Inject
	private LayerZSendBadResponse badResponse;
	
	private final Cache<SocketAddress, HttpClientSocket> cache;
	
	public Layer4Processor() {
		cache = CacheBuilder.newBuilder()
			    .concurrencyLevel(4)
			    .maximumSize(10000)
			    .expireAfterAccess(3, TimeUnit.MINUTES)
			    .removalListener(new SocketExpiredListener())
			    .build();
	}
	
	@Override
    public void incomingRequest(HttpRequest req, RequestId requestId, boolean isComplete, ResponseSender sender) {
		log.info("incoming request. channel="+ sender +"=\n"+req);
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
	
		//need synchronization if two clients of proxy access same httpSocket/addr!!!
		HttpClientSocket socket = cache.getIfPresent(addr);
		if(socket != null) {
			sendData(sender, socket.getRequestSender(), req);
		} else {
			openAndConnectSocket(addr, req, sender);
		}
	}

	private void sendData(ResponseSender channel, RequestSender requestSender, HttpRequest req) {
		// Can only deal with complete requests
		requestSender.sendRequest(req, true, new Layer1Response(layer2Processor, channel, req));
	}

    @Override
    public CompletableFuture<Void> incomingData(DataWrapper data, RequestId id, boolean isComplete, ResponseSender sender) {
        // current we don't handle chunked requests. TODO: deal with chunked requests
        throw new UnsupportedOperationException();
    }

    private HttpClientSocket openAndConnectSocket(InetSocketAddress addr, HttpRequest req, ResponseSender channel) {
		HttpClientSocket socket = httpClient.openHttpSocket(""+addr.getHostName()+"-"+addr.getPort(), new Layer1CloseListener(addr));
		log.info("connecting to addr="+addr);
		socket.connect(addr)
				.thenAccept(requestListener -> {
					sendData(channel, requestListener, req);
					cache.put(addr, socket);
				})
				.exceptionally(e -> layer2Processor.processError(channel, req, e));
		
		return socket;
	}

	@Override
	public void clientOpenChannel(HttpSocket HttpSocket) {
		log.info("browser client open channel");
	}
	
	@Override
	public void clientClosedChannel(HttpSocket httpSocket) {
		log.info("browser client closed channel");
	}

	private class SocketExpiredListener implements RemovalListener<SocketAddress, HttpClientSocket> {
		@Override
		public void onRemoval(RemovalNotification<SocketAddress, HttpClientSocket> notification) {
			log.info("closing socket="+notification.getKey()+".  cache removal cause="+notification.getCause());
			HttpClientSocket socket = notification.getValue();
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

	@Override
	public void incomingError(HttpException exc, HttpSocket httpSocket) {
		badResponse.sendServerResponse(((HttpServerSocket) httpSocket).getResponseSender(), exc);
	}

	@Override
	public void applyWriteBackPressure(ResponseSender responseSender) {
		log.error("NEED APPLY BACKPRESSURE", new RuntimeException());
	}

	@Override
	public void releaseBackPressure(ResponseSender sender) {
	}

}
