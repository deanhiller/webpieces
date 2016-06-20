package org.webpieces.webserver.impl;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.webpieces.frontend.api.FrontendSocket;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.HttpRequestMethod;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.httpparser.api.dto.UrlInfo;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.dto.RouterRequest;

public class RequestReceiver implements HttpRequestListener {

	@Inject
	private RoutingService routingService;
	
	@Override
	public void processHttpRequests(FrontendSocket channel, HttpRequest req, boolean isHttps) {
		ResponseStreamer streamer = new ProxyResponse(req, channel);
		
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
	public void sendServerResponse(FrontendSocket channel, Throwable exc, KnownStatusCode status) {
		
	}

	@Override
	public void clientClosedChannel(FrontendSocket channel) {
	}

	@Override
	public void applyWriteBackPressure(FrontendSocket channel) {
	}

	@Override
	public void releaseBackPressure(FrontendSocket channel) {
	}

}
