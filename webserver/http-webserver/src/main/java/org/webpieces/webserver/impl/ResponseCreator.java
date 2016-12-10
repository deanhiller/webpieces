package org.webpieces.webserver.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RouterCookie;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.common.ResponseCookie;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.HttpResponseStatus;
import org.webpieces.httpparser.api.dto.HttpResponseStatusLine;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.httpparser.api.subparsers.HeaderPriorityParser;
import org.webpieces.router.api.exceptions.CookieTooLargeException;
import org.webpieces.router.impl.CookieTranslator;
import org.webpieces.router.impl.compression.MimeTypes;
import org.webpieces.router.impl.compression.MimeTypes.MimeTypeResult;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
@Singleton
public class ResponseCreator {

	private static final Logger log = LoggerFactory.getLogger(ResponseCreator.class);
	private static final HeaderPriorityParser httpSubParser = HttpParserFactory.createHeaderParser();
	private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("E, dd MMM Y HH:mm:ss");

	@Inject
	private CookieTranslator cookieTranslator;
	@Inject
	private MimeTypes mimeTypes;
	
	public ResponseEncodingTuple createResponse(HttpRequest request, KnownStatusCode statusCode, String extension, String defaultMime) {
		MimeTypeResult mimeType = mimeTypes.extensionToContentType(extension, defaultMime);
		
		HttpResponseStatus status = new HttpResponseStatus();
		status.setKnownStatus(statusCode);
		
		HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
		statusLine.setStatus(status);
		HttpResponse response = new HttpResponse();
		response.setStatusLine(statusLine);
		
		response.addHeader(new Header(KnownHeaderName.CONTENT_TYPE, mimeType.mime));

		addCommonHeaders(request, response);
		return new ResponseEncodingTuple(response, mimeType);
	}
	
	public static class ResponseEncodingTuple {
		public HttpResponse response;
		public MimeTypeResult mimeType;

		public ResponseEncodingTuple(HttpResponse response, MimeTypeResult mimeType) {
			this.response = response;
			this.mimeType = mimeType;
		}	
	}
	
	public void addCommonHeaders(HttpRequest request, HttpResponse response) {
		KnownStatusCode statusCode = response.getStatusLine().getStatus().getKnownStatus();
		Header connHeader = request.getHeaderLookupStruct().getHeader(KnownHeaderName.CONNECTION);
		
		DateTime now = DateTime.now().toDateTime(DateTimeZone.UTC);
		String dateStr = formatter.print(now)+" GMT";

		//in general, nearly all these headers are desired..
		Header date = new Header(KnownHeaderName.DATE, dateStr);
		response.addHeader(date);

//		Header xFrame = new Header("X-Frame-Options", "SAMEORIGIN");
//		response.addHeader(xFrame);
		
		List<RouterCookie> cookies = createCookies(statusCode);
		for(RouterCookie c : cookies) {
			Header cookieHeader = create(c);
			response.addHeader(cookieHeader);
		}
		
		//X-XSS-Protection: 1; mode=block
		//X-Frame-Options: SAMEORIGIN
	    //Expires: Mon, 20 Jun 2016 02:33:52 GMT\r\n
	    //Cache-Control: private, max-age=31536000\r\n
	    //Last-Modified: Mon, 02 Apr 2012 02:13:37 GMT\r\n
		//X-Content-Type-Options: nosniff\r\n
		
		if(connHeader == null)
			return;
		else if(!"keep-alive".equals(connHeader.getValue()))
			return;

		//just re-use the connHeader from the request...
		response.addHeader(connHeader);
	}
	
	private List<RouterCookie> createCookies(KnownStatusCode statusCode) {
		if(!Current.isContextSet())
			return new ArrayList<>(); //in some exceptional cases like incoming cookies failing to parse, there will be no cookies
		
		try {
			List<RouterCookie> cookies = new ArrayList<>();
			cookieTranslator.addScopeToCookieIfExist(cookies, Current.flash());
			cookieTranslator.addScopeToCookieIfExist(cookies, Current.validation());
			cookieTranslator.addScopeToCookieIfExist(cookies, Current.session());
			return cookies;
		} catch(CookieTooLargeException e) {
			if(statusCode != KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR)
				throw e;
			//ELSE this is the second time we are rendering a response AND it was MOST likely caused by the same
			//thing when we tried to marshal out cookies to strings and they were too big, sooooooooooo in this
			//case, clear the cookie that failed.  One log of this should have already occurred but just in case
			//add one more log here but not with stack trace(so we don't get the annoying double stack trace on
			//failing. (The throws above is logged in catch statement elsewhere)
			log.error("Could not marshal cookie on http 500.  msg="+e.getMessage());
			return new ArrayList<>();
		}
	}
	
	private Header create(RouterCookie c) {
		ResponseCookie cookie = new ResponseCookie();
		cookie.setName(c.name);
		cookie.setValue(c.value);
		cookie.setDomain(c.domain);
		cookie.setPath(c.path);
		cookie.setMaxAgeSeconds(c.maxAgeSeconds);
		cookie.setSecure(c.isSecure);
		cookie.setHttpOnly(c.isHttpOnly);
		return httpSubParser.createHeader(cookie);
	}

	public void addDeleteCookie(HttpResponse response, String badCookieName) {
		RouterCookie cookie = cookieTranslator.createDeleteCookie(badCookieName);
		Header cookieHeader = create(cookie);
		response.addHeader(cookieHeader);
	}
}
