package org.webpieces.webserver.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Provider;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.BufferPool;
import org.webpieces.router.api.RouterService;
import org.webpieces.util.urlparse.UrlEncodedParser;
import org.webpieces.webserver.api.WebServerConfig;
import org.webpieces.webserver.impl.body.BodyParsers;

public class RequestHelpFacade {

	
	@Inject
	private RouterService routingService;
	@Inject
	private WebServerConfig config;
	@Inject
	private UrlEncodedParser urlEncodedParser;
	@Inject
	private BufferPool bufferPool;
	@Inject
	private BodyParsers bodyParsers;
	
	//I don't use javax.inject.Provider much as reflection creation is a tad slower but screw it......(it's fast enough)..AND
	//it keeps the code a bit more simple.  We could fix this later
	@Inject
	private Provider<ProxyResponse> responseProvider;
	
	public void urlEncodeParse(String postfix, RouterRequest routerRequest) {
		urlEncodedParser.parse(postfix, (k, v) -> addToMap(k,v,routerRequest.queryParams));
	}
	
	private Void addToMap(String k, String v, Map<String, List<String>> queryParams) {
		List<String> list = queryParams.get(k);
		if(list == null) {
			list = new ArrayList<>();
			queryParams.put(k, list);
		}
		
		list.add(v);
		return null;
	}

	public BufferPool getBufferPool() {
		return bufferPool;
	}

	public CompletableFuture<Void> incomingCompleteRequest(RouterRequest routerRequest, ProxyResponse streamer) {
		return routingService.incomingCompleteRequest(routerRequest, streamer);
	}

	public WebServerConfig getConfig() {
		return config;
	}

	public ProxyResponse createProxyResponse() {
		return responseProvider.get();
	}

	public BodyParsers getBodyParsers() {
		return bodyParsers;
	}

}
