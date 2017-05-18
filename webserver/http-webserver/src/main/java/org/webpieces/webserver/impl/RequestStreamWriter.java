package org.webpieces.webserver.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.AcceptMediaType;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RouterCookie;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend2.api.FrontendStream;
import org.webpieces.frontend2.api.SocketInfo;
import org.webpieces.router.api.exceptions.BadCookieException;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.hpack.api.dto.Http2HeaderStruct;
import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.subparsers.AcceptType;
import com.webpieces.hpack.api.subparsers.HeaderPriorityParser;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class RequestStreamWriter implements StreamWriter {

	private static final Logger log = LoggerFactory.getLogger(RequestStreamWriter.class);
	private static final HeaderPriorityParser headerParser = HpackParserFactory.createHeaderParser();
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private static Set<Http2HeaderName> headersSupported = new HashSet<>();

	static {
		//We keep this list in place to log out what we have not implemented yet.  This allows us to see if
		//we missed anything on the request side.
		headersSupported.add(Http2HeaderName.METHOD);
		headersSupported.add(Http2HeaderName.PATH);
		headersSupported.add(Http2HeaderName.AUTHORITY);
		headersSupported.add(Http2HeaderName.SCHEME);

		headersSupported.add(Http2HeaderName.DATE);
		headersSupported.add(Http2HeaderName.CONNECTION);
		headersSupported.add(Http2HeaderName.USER_AGENT);
		headersSupported.add(Http2HeaderName.CONTENT_LENGTH);
		headersSupported.add(Http2HeaderName.CONTENT_TYPE);
		headersSupported.add(Http2HeaderName.ACCEPT_ENCODING);
		headersSupported.add(Http2HeaderName.ACCEPT_LANGUAGE);
		headersSupported.add(Http2HeaderName.ACCEPT);
		headersSupported.add(Http2HeaderName.COOKIE);
		headersSupported.add(Http2HeaderName.REFERER);
		headersSupported.add(Http2HeaderName.ORIGIN);
		headersSupported.add(Http2HeaderName.CACHE_CONTROL);
		headersSupported.add(Http2HeaderName.PRAGMA);
		headersSupported.add(Http2HeaderName.X_REQUESTED_WITH);
		
		//we don't do redirects or anything like that yet...
		headersSupported.add(Http2HeaderName.UPGRADE_INSECURE_REQUESTS);
	}

	private RequestHelpFacade facade;
	private FrontendStream stream;
	private Http2Headers requestHeaders;

	private CompletableFuture<Void> outstandingRequest;
	private DataWrapper data = dataGen.emptyWrapper();
	private boolean cancelled;
	private SocketInfo socketInfo;

	public RequestStreamWriter(RequestHelpFacade facade, FrontendStream stream, Http2Headers headers, SocketInfo socketInfo) {
		this.facade = facade;
		this.stream = stream;
		this.requestHeaders = headers;
		this.socketInfo = socketInfo;
	}
	
	@Override
	public CompletableFuture<StreamWriter> send(PartialStream frame) {
		if(cancelled)
			return CompletableFuture.completedFuture(this);
		else if(frame instanceof DataFrame) {
			DataFrame dataFrame = (DataFrame) frame;
			data = dataGen.chainDataWrappers(data, dataFrame.getData());
			if(frame.isEndOfStream())
				outstandingRequest = handleCompleteRequest();
			return CompletableFuture.completedFuture(this);
		} else if(frame instanceof Http2Headers) {
			if(frame.isEndOfStream())
				outstandingRequest = handleCompleteRequest();			
			return CompletableFuture.completedFuture(this);
		}
		
		throw new IllegalStateException("frame not expected="+frame);
	}

	CompletableFuture<Void> handleCompleteRequest() {
		for(Http2Header h : requestHeaders.getHeaders()) {
			if (!headersSupported.contains(h.getKnownName()))
				log.error("This webserver has not thought about supporting header="
						+ h.getName() + " quite yet.  value=" + h.getValue() + " Please let us know and we can quickly add support");
		}

		RouterRequest routerRequest = new RouterRequest();
		routerRequest.orginalRequest = requestHeaders;
		
		//TODO(dhiller): figure out the firewall way to config when firewall terminates the ssl and we receive http
		//or the secure routes will not show up
		routerRequest.isHttps = socketInfo.isHttps();

		Http2Header header = requestHeaders.getHeaderLookupStruct().getHeader(Http2HeaderName.AUTHORITY);
		if(header == null) {
			throw new IllegalArgumentException("Must contain Host header");
		}
		String domain = header.getValue();

		int port = 80;
        if(routerRequest.isHttps)
                port = 443;
        //if there is a firewall this port is wrong....and the above or below is right
		//int port = socketInfo.getLocalBoundAddress().getPort();
		
		int index2 = domain.indexOf(":");
		//host header may have port in it format is user@domain:port where user and port are optional
		//TODO(dhiller): find when user is used and test implement
		if(index2 >= 0) {
			port = Integer.parseInt(domain.substring(index2+1));
			domain = domain.substring(0, index2);
		}
		

		Http2Header methodHeader = requestHeaders.getHeaderLookupStruct().getHeader(Http2HeaderName.METHOD);
		
		HttpMethod method = HttpMethod.lookup(methodHeader.getValue());
		if(method == null)
			throw new UnsupportedOperationException("method not supported="+methodHeader);

		parseCookies(requestHeaders, routerRequest);
		parseAcceptLang(requestHeaders, routerRequest);
		parseAccept(requestHeaders, routerRequest);
		routerRequest.encodings = headerParser.parseAcceptEncoding(requestHeaders);

		Http2Header referHeader = requestHeaders.getHeaderLookupStruct().getHeader(Http2HeaderName.REFERER);
		if(referHeader != null)
			routerRequest.referrer = referHeader.getValue().trim();

		Http2Header xRequestedWithHeader = requestHeaders.getHeaderLookupStruct().getHeader(Http2HeaderName.X_REQUESTED_WITH);
		if(xRequestedWithHeader != null && "XMLHttpRequest".equals(xRequestedWithHeader.getValue().trim()))
			routerRequest.isAjaxRequest = true;
		
		Http2Header pathHeader = requestHeaders.getHeaderLookupStruct().getHeader(Http2HeaderName.PATH);

		
		parseBody(requestHeaders, routerRequest);
		routerRequest.method = method;
		routerRequest.domain = domain;
		routerRequest.port = port;
		String fullPath = pathHeader.getValue();
		int index = fullPath.indexOf("?");
		if(index > 0) {
			routerRequest.relativePath = fullPath.substring(0, index);
			String postfix = fullPath.substring(index+1);
			facade.urlEncodeParse(postfix, routerRequest);
		} else {
			routerRequest.queryParams = new HashMap<>();
			routerRequest.relativePath = fullPath;
		}

		//http1.1 so no...
		routerRequest.isSendAheadNextResponses = false;
		if(routerRequest.relativePath.contains("?"))
			throw new UnsupportedOperationException("not supported yet");

		ProxyResponse streamer = facade.createProxyResponse();
		try {
			streamer.init(routerRequest, requestHeaders, stream, facade.getBufferPool());

			return facade.incomingCompleteRequest(routerRequest, streamer);
		} catch (BadCookieException e) {
			log.warn("This occurs if secret key changed, or you booted another webapp with different key on same port or someone modified the cookie", e);
			streamer.sendRedirectAndClearCookie(routerRequest, e.getCookieName());
			return CompletableFuture.completedFuture(null);
		}
	}
	
	private void parseAccept(Http2Headers req, RouterRequest routerRequest) {
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

	private void parseAcceptLang(Http2Headers req, RouterRequest routerRequest) {
		List<Locale> headerItems = headerParser.parseAcceptLangFromRequest(req);
		
		//tack on DefaultLocale if not there..
		if(!headerItems.contains(facade.getConfig().getDefaultLocale()))
			headerItems.add(facade.getConfig().getDefaultLocale());
		
		routerRequest.preferredLocales = headerItems;
	}

	private void parseCookies(Http2Headers req, RouterRequest routerRequest) {
		//http://stackoverflow.com/questions/16305814/are-multiple-cookie-headers-allowed-in-an-http-request
		Map<String, String> cookies = headerParser.parseCookiesFromRequest(req);
		routerRequest.cookies = copy(cookies);
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

	private void parseBody(Http2Headers req, RouterRequest routerRequest) {
		Http2HeaderStruct headers = req.getHeaderLookupStruct();
		Http2Header lengthHeader = headers.getHeader(Http2HeaderName.CONTENT_LENGTH);
		Http2Header typeHeader = headers.getHeader(Http2HeaderName.CONTENT_TYPE);

		routerRequest.body = data;

		if(lengthHeader != null) {
			//Integer.parseInt(lengthHeader.getValue()); should not fail as it would have failed earlier in the parser when
			//reading in the body
			routerRequest.contentLengthHeaderValue = Integer.parseInt(lengthHeader.getValue());
		} 
		
		if(typeHeader != null) {
			routerRequest.contentTypeHeaderValue = typeHeader.getValue();
		}
	}

	public void setOutstandingRequest(CompletableFuture<Void> future) {
		this.outstandingRequest = future;
	}

	public void cancelOutstandingRequest() {
		cancelled = true;
//		if(outstandingRequest != null)
//			outstandingRequest.cancel(true);
	}

}
