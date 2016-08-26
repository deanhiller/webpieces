package org.webpieces.webserver.impl;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.AcceptMediaType;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RouterCookie;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.frontend.api.FrontendSocket;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.frontend.api.exception.HttpException;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.Headers;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.UrlInfo;
import org.webpieces.httpparser.api.subparsers.AcceptType;
import org.webpieces.httpparser.api.subparsers.HeaderPriorityParser;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RoutingService;
import org.webpieces.templating.api.TemplateService;
import org.webpieces.webserver.api.WebServerConfig;
import org.webpieces.webserver.impl.parsing.BodyParser;
import org.webpieces.webserver.impl.parsing.BodyParsers;
import org.webpieces.webserver.impl.parsing.FormUrlEncodedParser;

public class RequestReceiver implements HttpRequestListener {
	
	private static final Logger log = LoggerFactory.getLogger(RequestReceiver.class);
	private static final HeaderPriorityParser headerParser = HttpParserFactory.createHeaderParser();
	
	@Inject
	private RoutingService routingService;
	@Inject
	private TemplateService templatingService;
	@Inject
	private WebServerConfig config;
	@Inject
	private FormUrlEncodedParser parser = new FormUrlEncodedParser();
	
	private Set<String> headersSupported = new HashSet<>();
	
	public RequestReceiver() {
		headersSupported.add(KnownHeaderName.HOST.getHeaderName().toLowerCase());
		headersSupported.add(KnownHeaderName.DATE.getHeaderName().toLowerCase());
		headersSupported.add(KnownHeaderName.CONNECTION.getHeaderName().toLowerCase());
		headersSupported.add(KnownHeaderName.USER_AGENT.getHeaderName().toLowerCase());
		headersSupported.add(KnownHeaderName.CONTENT_LENGTH.getHeaderName().toLowerCase());
		headersSupported.add(KnownHeaderName.CONTENT_TYPE.getHeaderName().toLowerCase());
		headersSupported.add(KnownHeaderName.ACCEPT_LANGUAGE.getHeaderName().toLowerCase());
		headersSupported.add(KnownHeaderName.ACCEPT.getHeaderName().toLowerCase());
		headersSupported.add(KnownHeaderName.COOKIE.getHeaderName().toLowerCase());
	}
	
	@Override
	public void processHttpRequests(FrontendSocket channel, HttpRequest req, boolean isHttps) {
		//log.info("request received on channel="+channel);
		ResponseStreamer streamer = new ProxyResponse(req, channel, routingService, templatingService, config);
		
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

		parseCookies(req, routerRequest);
		parseAcceptLang(req, routerRequest);
		parseAccept(req, routerRequest);
		
		parseBody(req, routerRequest);
		routerRequest.method = method;
		routerRequest.domain = value;
		String fullPath = uriInfo.getFullPath();
		int index = fullPath.indexOf("?");
		if(index > 0) {
			routerRequest.relativePath = fullPath.substring(0, index);
			String postfix = fullPath.substring(index+1);
			parser.parse(postfix, (k, v) -> addToMap(k,v,routerRequest.queryParams));
		} else {
			routerRequest.queryParams = new HashMap<>();
			routerRequest.relativePath = fullPath;	
		}
		
		//http1.1 so no...
		routerRequest.isSendAheadNextResponses = false;
		if(routerRequest.relativePath.contains("?"))
			throw new UnsupportedOperationException("not supported yet");
		
		routingService.processHttpRequests(routerRequest, streamer );
	}


	private void parseAccept(HttpRequest req, RouterRequest routerRequest) {
		List<AcceptType> types = headerParser.parseAcceptFromRequest(req);
		List<AcceptMediaType> acceptedTypes = new ArrayList<>();
		
		for(AcceptType t : types) {
			if(t.isMatchesAllTypes())
				acceptedTypes.add(new AcceptMediaType());
			else if(t.isMatchesAllSubtypes())
				acceptedTypes.add(new AcceptMediaType(t.getMainType()));
			else
				acceptedTypes.add(new AcceptMediaType(t.getMainType(), t.getSubType()));
		}
		
		routerRequest.acceptedTypes = acceptedTypes;
	}

	private void parseAcceptLang(HttpRequest req, RouterRequest routerRequest) {
		List<Locale> headerItems = headerParser.parseAcceptLangFromRequest(req);
		
		//tack on DefaultLocale if not there..
		if(!headerItems.contains(config.getDefaultLocale()))
			headerItems.add(config.getDefaultLocale());
		
		routerRequest.preferredLocales = headerItems;
	}

	private void parseCookies(HttpRequest req, RouterRequest routerRequest) {
		//http://stackoverflow.com/questions/16305814/are-multiple-cookie-headers-allowed-in-an-http-request
		Map<String, String> cookies = headerParser.parseCookiesFromRequest(req);
		routerRequest.cookies = copy(cookies);
	}

	private String addToMap(String k, String v, Map<String, List<String>> queryParams) {
		List<String> list = queryParams.get(k);
		if(list == null) {
			list = new ArrayList<>();
			queryParams.put(k, list);
		}
		
		list.add(v);
		return null;
	}

	private Map<String, RouterCookie> copy(Map<String, String> cookies) {
		Map<String, RouterCookie> map = new HashMap<>();
		for(Entry<String, String> entry : cookies.entrySet()) {
			RouterCookie c = copy(entry.getKey(), entry.getValue());
			map.put(c.name, c);
		}
		return map;
	}

	private RouterCookie copy(String name, String val) {
		RouterCookie rCookie = new RouterCookie();
		rCookie.name = name;
		rCookie.value = val;
		return rCookie;
	}

	private void parseBody(HttpRequest req, RouterRequest routerRequest) {
		Headers headers = req.getHeaderLookupStruct();
		Header lengthHeader = headers.getHeader(KnownHeaderName.CONTENT_LENGTH);
		Header typeHeader = headers.getHeader(KnownHeaderName.CONTENT_TYPE);
		if(lengthHeader == null)
			return;
		else if(typeHeader == null) {
			log.error("Incoming content length was specified, but no contentType was(We will treat like there was no body at all).  req="+req);
			return;
		}
		
		BodyParser parser = BodyParsers.lookup(typeHeader.getValue());
		if(parser == null) {
			log.error("Incoming content length was specified but content type was not 'application/x-www-form-urlencoded'(We will treat like there was no body at all).  req="+req);
			return;			
		}

		DataWrapper body = req.getBody();
		Charset encoding = config.getDefaultFormAcceptEncoding();
		parser.parse(body, routerRequest, encoding);
	}

	@Override
	public void sendServerResponse(FrontendSocket channel, HttpException exc) {
		//If status is a 4xx, send it back to the client with just raw information
		
		//If status is a 5xx, send it into the routingService to be displayed back to the user
		
		log.error("Need to clean this up and render good 500 page for real bugs. thread="+Thread.currentThread().getName(), exc);
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

}
