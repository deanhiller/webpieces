package org.webpieces.webserver.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.frontend.api.FrontendSocket;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.frontend.api.exception.HttpException;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.UrlInfo;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.dto.RouterRequest;
import org.webpieces.templating.api.ReverseUrlLookup;
import org.webpieces.templating.api.TemplateService;
import org.webpieces.webserver.api.WebServerConfig;

public class RequestReceiver implements HttpRequestListener {
	
	private static final Logger log = LoggerFactory.getLogger(RequestReceiver.class);
	@Inject
	private RoutingService routingService;
	@Inject
	private TemplateService templatingService;
	@Inject
	private WebServerConfig config;
	private UrlLookup lookup = new UrlLookup();
	
	private Set<String> headersSupported = new HashSet<>();
	
	public RequestReceiver() {
		headersSupported.add(KnownHeaderName.HOST.getHeaderName().toLowerCase());
		headersSupported.add(KnownHeaderName.DATE.getHeaderName().toLowerCase());
		headersSupported.add(KnownHeaderName.CONNECTION.getHeaderName().toLowerCase());
		headersSupported.add(KnownHeaderName.USER_AGENT.getHeaderName().toLowerCase());
		
	}
	
	@Override
	public void processHttpRequests(FrontendSocket channel, HttpRequest req, boolean isHttps) {
		//log.info("request received on channel="+channel);
		ResponseStreamer streamer = new ProxyResponse(req, channel, lookup, templatingService, config);
		
		for(Header h : req.getHeaders()) {
			if(!headersSupported.contains(h.getName().toLowerCase()))
				log.error("This webserver has not thought about supporting header="
						+h.getName()+" quite yet.  value="+h.getValue()+" Please let us know and we can quickly add support");
		}
		
		RouterRequest routerRequest = new RouterRequest();
		routerRequest.isHttps = isHttps;
		Header header = req.getHeaderLookupStruct().getHeader(KnownHeaderName.HOST);
		if(header == null) {
			throw new IllegalArgumentException("Must contain Host header");
		}
		String value = header.getValue();
		HttpRequestLine requestLine = req.getRequestLine();
		UrlInfo uriInfo = requestLine.getUri().getUriBreakdown();
		
		HttpMethod method = HttpMethod.lookup(requestLine.getMethod().getMethodAsString());
		if(method == null)
			throw new UnsupportedOperationException("method not supported="+requestLine.getMethod().getMethodAsString());

		routerRequest.method = method;
		routerRequest.domain = value;
		routerRequest.relativePath = uriInfo.getFullPath(); 
		//http1.1 so no...
		routerRequest.isSendAheadNextResponses = false;
		if(routerRequest.relativePath.contains("?"))
			throw new UnsupportedOperationException("not supported yet");
				
		routingService.processHttpRequests(routerRequest, streamer );
	}

	@Override
	public void sendServerResponse(FrontendSocket channel, HttpException exc) {
		//If status is a 4xx, send it back to the client with just raw information
		
		//If status is a 5xx, send it into the routingService to be displayed back to the user
		
		log.error("Need to clean this up and render good 500 page for real bugs. thread="+Thread.currentThread().getName());
		ProxyResponse proxyResp = new ProxyResponse(channel);
		proxyResp.sendFailure(exc);
	}

	@Override
	public void clientOpenChannel(FrontendSocket channel) {
		log.info("browser client open channel="+channel);
	}
	
	@Override
	public void clientClosedChannel(FrontendSocket channel) {
		log.info("browser client closed channel="+channel);
	}

	@Override
	public void applyWriteBackPressure(FrontendSocket channel) {
	}

	@Override
	public void releaseBackPressure(FrontendSocket channel) {
	}

	private class UrlLookup implements ReverseUrlLookup {
		@Override
		public String fetchUrl(String routeId, Map<String, String> args) {
			return routingService.convertToUrl(routeId, args);
		}
	}
}
