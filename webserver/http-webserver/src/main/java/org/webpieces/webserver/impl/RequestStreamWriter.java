package org.webpieces.webserver.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.webpieces.ctx.api.AcceptMediaType;
import org.webpieces.ctx.api.ContentType;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RouterCookie;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.ctx.api.UriInfo;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend2.api.ResponseStream;
import org.webpieces.router.api.exceptions.BadCookieException;
import org.webpieces.util.futures.FutureHelper;
import org.webpieces.webserver.impl.body.BodyParser;
import org.webpieces.webserver.impl.body.BodyParsers;

import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.subparsers.AcceptType;
import com.webpieces.hpack.api.subparsers.HeaderPriorityParser;
import com.webpieces.hpack.api.subparsers.ParsedContentType;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

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
		headersSupported.add(Http2HeaderName.X_FORWARDED_PROTO);
		
		//we don't do redirects or anything like that yet...
		headersSupported.add(Http2HeaderName.UPGRADE_INSECURE_REQUESTS);
	}

	private Random random = new Random();
	private RequestHelpFacade facade;
	private ResponseStream stream;
	private Http2Request requestHeaders;

	private volatile CompletableFuture<Void> outstandingRequest = CompletableFuture.completedFuture(null); //prevent nullPointers
	private DataWrapper data = dataGen.emptyWrapper();
	private boolean cancelled;
	private FutureHelper futureUtil;

	public RequestStreamWriter(RequestHelpFacade facade, ResponseStream stream, Http2Request headers) {
		this.facade = facade;
		this.stream = stream;
		this.requestHeaders = headers;
		this.futureUtil = facade.getFutureUtil();
	}
	
	@Override
	public CompletableFuture<Void> processPiece(StreamMsg frame) {
		
		if(cancelled)
			return CompletableFuture.completedFuture(null);
		else if(frame instanceof DataFrame) {
			DataFrame dataFrame = (DataFrame) frame;
			data = dataGen.chainDataWrappers(data, dataFrame.getData());
			if(frame.isEndOfStream()) {
				CompletableFuture<Void> outstandingReq = handleCompleteRequest();
				setOutstandingRequest(outstandingReq);
			}
			return CompletableFuture.completedFuture(null);
		} else if(frame instanceof Http2Headers) {
			if(frame.isEndOfStream()) {
				CompletableFuture<Void> outstandingReq = handleCompleteRequest();
				setOutstandingRequest(outstandingReq);
			}
			return CompletableFuture.completedFuture(null);
		}
		
		throw new IllegalStateException("frame not expected="+frame);
	}

	CompletableFuture<Void> handleCompleteRequest() {
		MDC.put("txId", generate());
		return futureUtil.finallyBlock(
				() -> handleCompleteRequestImpl(), 
				() -> MDC.put("txId", null)
		);
	}

	public String generate() {
		String randomTxId = ""+random.nextInt(1_000_000);
		if(randomTxId.length() < 6)
			randomTxId = "00000000"+randomTxId;
		int len = randomTxId.length();
		String s1 = randomTxId.substring(len-6, len-3);
		String s2 = randomTxId.substring(len-3, len);
		return s1+"-"+s2; //human readable instance id
	}

	CompletableFuture<Void> handleCompleteRequestImpl() {
		for(Http2Header h : requestHeaders.getHeaders()) {
			if (!headersSupported.contains(h.getKnownName()))
				log.debug("This webserver has not thought about supporting header="
						+ h.getName() + " quite yet.  value=" + h.getValue() + " Please let us know and we can quickly add support");
		}

		RouterRequest routerRequest = new RouterRequest();
		fillInRouterRequest(routerRequest);
		
		if(log.isDebugEnabled())
			log.debug("received request="+requestHeaders+" routerRequest="+routerRequest);

		ProxyResponse streamer = null;
		try {
			streamer = facade.createProxyResponse();
			streamer.init(routerRequest, requestHeaders, stream, facade.getMaxBodySizeToSend());
		} catch(Throwable e) {
			log.error("FATAL, cannot respond since could not create proxy response", e);
			return CompletableFuture.completedFuture(null);
		}
		
		try {
			return facade.incomingCompleteRequest(routerRequest, streamer);
		} catch (BadCookieException e) {
			log.warn("This occurs if secret key changed, or you booted another webapp with different key on same port or someone modified the cookie", e);
			streamer.sendRedirectAndClearCookie(routerRequest, e.getCookieName());
			return CompletableFuture.completedFuture(null);
		}
	}

	//TODO(dhiller): This code now exists in TWO locations.  modify to share SAME code instead of copying
	public UriInfo getUriBreakdown(String uri) {
	    int doubleslashIndex = uri.indexOf("://");
	    if(doubleslashIndex == -1)
	    	return new UriInfo(uri);
	    
	    int domainStartIndex = doubleslashIndex+3;
	    String prefix = uri.substring(0, doubleslashIndex);
	    Integer port  = null;
	    
	    String path = "";
	    int firstSlashIndex = uri.indexOf('/', domainStartIndex);
	    if(firstSlashIndex < 0) {
	    	firstSlashIndex = uri.length();
	    	path = "/";
	    } else {
	    	path = uri.substring(firstSlashIndex);
	    }

	    
	    
	    int domainEndIndex = firstSlashIndex;
	    int portIndex = uri.indexOf(':', domainStartIndex);
	    if(portIndex > 0 && portIndex < firstSlashIndex) {
	    	domainEndIndex = portIndex;
	    	String portStr = uri.substring(portIndex+1, firstSlashIndex);
	    	port = convert(portStr, uri);
	    }
	    	
	    String host = uri.substring(domainStartIndex, domainEndIndex);

	    return new UriInfo(prefix, host, port, path);
	}
	
	private Integer convert(String portStr, String uri2) {
		try {
			return Integer.parseInt(portStr);
		} catch(NumberFormatException e) {
			throw new IllegalStateException("port in uri="+uri2+" is not an integer", e);
		}
	}
	
	private void fillInRouterRequest(RouterRequest routerRequest) {
		routerRequest.orginalRequest = requestHeaders;
		
		fillInHttpsValue(routerRequest);
		
		routerRequest.isBackendRequest = stream.getSocket().isBackendSocket();

		String domain = requestHeaders.getAuthority();
		if(domain == null) {
			throw new IllegalArgumentException("Must contain Host(http1.1) or :authority(http2) header");
		}

		int port = 80;
        if(routerRequest.isHttps)
                port = 443;

		
		int index2 = domain.indexOf(":");
		//host header may have port in it format is user@domain:port where user and port are optional
		//TODO(dhiller): find when user is used and test implement
		if(index2 >= 0) {
			port = Integer.parseInt(domain.substring(index2+1));
			domain = domain.substring(0, index2);
		}

        //if there is a firewall the socket's port is wrong(ie. the line below)....and the above is correct!!!
		//int port = socketInfo.getLocalBoundAddress().getPort();
		
		String methodString = requestHeaders.getMethodString();
		HttpMethod method = HttpMethod.lookup(methodString);
		if(method == null)
			throw new UnsupportedOperationException("method not supported="+methodString);

		parseCookies(requestHeaders, routerRequest);
		parseAcceptLang(requestHeaders, routerRequest);
		parseAccept(requestHeaders, routerRequest);
		routerRequest.encodings = headerParser.parseAcceptEncoding(requestHeaders);
		routerRequest.contentTypeHeaderValue = parse(requestHeaders);

		String referHeader = requestHeaders.getSingleHeaderValue(Http2HeaderName.REFERER);
		if(referHeader != null)
			routerRequest.referrer = referHeader;

		String xRequestedWithHeader = requestHeaders.getSingleHeaderValue(Http2HeaderName.X_REQUESTED_WITH);
		if("XMLHttpRequest".equals(xRequestedWithHeader))
			routerRequest.isAjaxRequest = true;
		
		String thePath = requestHeaders.getPath();
		if(thePath == null)
			throw new IllegalArgumentException(":path header(http2) or path in request line(http1.1) is required");
		
		UriInfo uriBreakdown = getUriBreakdown(thePath);
		String fullPath = uriBreakdown.getFullPath();
		routerRequest.requestUri = uriBreakdown;
		
		parseBody(requestHeaders, routerRequest);
		routerRequest.method = method;
		routerRequest.domain = domain;
		routerRequest.port = port;
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
	}

	private ContentType parse(Http2Request requestHeaders2) {
		ParsedContentType parsedType = headerParser.parseContentType(requestHeaders2);
		if(parsedType == null)
			return null;
		return new ContentType(parsedType.getMimeType(), parsedType.getCharSet(), parsedType.getBoundary(), parsedType.getFullValue());
	}

	private void fillInHttpsValue(RouterRequest routerRequest) {
		//There are two ways to terminate SSL, x-forwarded-proto header from firewall OR you can configure your
		//firewall to point to the https port(AND turn that https port so it is only http) so you have TWO http
		//ports open, one will always be https and the other http.  
		String header = requestHeaders.getSingleHeaderValue(Http2HeaderName.X_FORWARDED_PROTO);
		if("https".equals(header))
			routerRequest.isHttps = true;
		else if("http".equals(header))
			routerRequest.isHttps = false;
		else
			routerRequest.isHttps = stream.getSocket().isForServingHttpsPages();
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
		String lengthHeader = req.getSingleHeaderValue(Http2HeaderName.CONTENT_LENGTH);

		routerRequest.body = data;

		if(lengthHeader != null) {
			//Integer.parseInt(lengthHeader.getValue()); should not fail as it would have failed earlier in the parser when
			//reading in the body
			routerRequest.contentLengthHeaderValue = Integer.parseInt(lengthHeader);
		} 
		
		parseBodyFromContentType(routerRequest);
	}

	/**
	 * This has to be above LoginFilter so LoginFilter can flash the multiPartParams so edits exist through
	 * a login!!  This moves body to the muliPartParams Map which LoginFilter uses
	 */
	private void parseBodyFromContentType(RouterRequest req) {
		if(req.contentLengthHeaderValue == null)
			return;
		else if(req.contentLengthHeaderValue == 0)
			return;
		
		if(req.contentTypeHeaderValue == null) {
			log.info("Incoming content length was specified, but no contentType was(We will not parse the body).  req="+req+" httpReq="+req.orginalRequest);
			return;
		}
		
		BodyParsers requestBodyParsers = facade.getBodyParsers();
		
		BodyParser parser = requestBodyParsers.lookup(req.contentTypeHeaderValue.getContentType());
		if(parser == null) {
			log.error("Incoming content length was specified but content type was not 'application/x-www-form-urlencoded'(We will not parse body).  req="+req+" httpReq="+req.orginalRequest);
			return;
		}

		DataWrapper body = req.body;
		parser.parse(body, req);
	}
	
	public void setOutstandingRequest(CompletableFuture<Void> future) {
		this.outstandingRequest = future;
	}

	public void cancelOutstandingRequest() {
		cancelled = true;
		if(outstandingRequest != null) //should no longer ever be null
			outstandingRequest.cancel(true);
	}

}
