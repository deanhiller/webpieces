package org.webpieces.webserver.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.TwoPools;
import org.webpieces.router.api.RouterService;
import org.webpieces.util.futures.FutureHelper;
import org.webpieces.util.urlparse.UrlEncodedParser;
import org.webpieces.webserver.api.WebServerConfig;
import org.webpieces.webserver.impl.body.BodyParsers;

@Singleton
public class RequestHelpFacade implements StreamsWebManaged {

	private final RouterService routingService;
	private final WebServerConfig config;
	private final UrlEncodedParser urlEncodedParser;
	private final BodyParsers bodyParsers;
	private final FutureHelper futureUtil;
	
	//I don't use javax.inject.Provider much as reflection creation is a tad slower but screw it......(it's fast enough)..AND
	//it keeps the code a bit more simple.  We could fix this later
	private final Provider<ProxyResponse> responseProvider;
	
	//The max size of body for dynamic pages for Full responses and chunked responses.  This
	//is used to determine send chunks instead of full response as well since it won't fit
	//in full response sometimes
	private int maxBodySizeToSend = TwoPools.DEFAULT_MAX_BASE_BUFFER_SIZE;

	@Inject
	public RequestHelpFacade(RouterService routingService, WebServerConfig config, UrlEncodedParser urlEncodedParser,
			BodyParsers bodyParsers, Provider<ProxyResponse> responseProvider, FutureHelper futureUtil) {
		super();
		this.routingService = routingService;
		this.config = config;
		this.urlEncodedParser = urlEncodedParser;
		this.bodyParsers = bodyParsers;
		this.responseProvider = responseProvider;
		this.futureUtil = futureUtil;
	}

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
	
	@Override
	public String getCategory() {
		return "Webpieces Webserver";
	}

	@Override
	public int getMaxBodySizeToSend() {
		return maxBodySizeToSend;
	}

	@Override
	public void setMaxBodySizeSend(int maxBodySize) {
		this.maxBodySizeToSend = maxBodySize;
	}

	public FutureHelper getFutureUtil() {
		return futureUtil;
	}

}
