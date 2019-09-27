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
import org.webpieces.router.api.exceptions.CookieTooLargeException;
import org.webpieces.router.impl.CookieTranslator;
import org.webpieces.router.impl.compression.MimeTypes;
import org.webpieces.router.impl.compression.MimeTypes.MimeTypeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.hpack.api.subparsers.HeaderPriorityParser;
import com.webpieces.hpack.api.subparsers.ResponseCookie;
import com.webpieces.http2parser.api.dto.StatusCode;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;
@Singleton
public class ResponseCreator {

	private static final Logger log = LoggerFactory.getLogger(ResponseCreator.class);
	private static final HeaderPriorityParser httpSubParser = HpackParserFactory.createHeaderParser();
	private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("E, dd MMM Y HH:mm:ss");

	@Inject
	private CookieTranslator cookieTranslator;
	@Inject
	private MimeTypes mimeTypes;
	
	public ResponseEncodingTuple createResponse(Http2Request request, StatusCode statusCode,
			String extension, String defaultMime, boolean isDynamicPartOfWebsite) {
		MimeTypeResult mimeType = mimeTypes.extensionToContentType(extension, defaultMime);
		
		return createContentResponseImpl(request, statusCode.getCode(), statusCode.getReason(), isDynamicPartOfWebsite, mimeType);
	}

	public ResponseEncodingTuple createContentResponse(
			Http2Request request, int statusCode,
			String reason, MimeTypeResult mimeType) {
		return createContentResponseImpl(request, statusCode, reason, false, mimeType);
	}
	
	private ResponseEncodingTuple createContentResponseImpl(
			Http2Request request, 
			int statusCode,
			String reason,
			boolean isDynamicPartOfWebsite, 
			MimeTypeResult mimeType) {

		Http2Response response = new Http2Response();
		response.setEndOfStream(false);

		response.addHeader(new Http2Header(Http2HeaderName.STATUS, statusCode+""));
		response.addHeader(new Http2Header("reason", reason));
		response.addHeader(new Http2Header(Http2HeaderName.CONTENT_TYPE, mimeType.mime));

		boolean isInternalError = false;
		if(statusCode == 500)
			isInternalError = true;
		
		addCommonHeaders(request, response, isInternalError, isDynamicPartOfWebsite);
		return new ResponseEncodingTuple(response, mimeType);
	}
	
	public static class ResponseEncodingTuple {
		public Http2Response response;
		public MimeTypeResult mimeType;

		public ResponseEncodingTuple(Http2Response response, MimeTypeResult mimeType) {
			this.response = response;
			this.mimeType = mimeType;
		}	
	}
	
	public void addCommonHeaders(Http2Request request, Http2Response response, boolean isInternalError, boolean isDynamicPartOfWebsite) {
		String connHeader = request.getSingleHeaderValue(Http2HeaderName.CONNECTION);
		
		DateTime now = DateTime.now().toDateTime(DateTimeZone.UTC);
		String dateStr = formatter.print(now)+" GMT";

		//in general, nearly all these headers are desired..
		Http2Header date = new Http2Header(Http2HeaderName.DATE, dateStr);
		response.addHeader(date);

//		Header xFrame = new Header("X-Frame-Options", "SAMEORIGIN");
//		response.addHeader(xFrame);
		
		if(isDynamicPartOfWebsite) {
			List<RouterCookie> cookies = createCookies(isInternalError);
			for(RouterCookie c : cookies) {
				Http2Header cookieHeader = create(c);
				response.addHeader(cookieHeader);
			}
		}
		
		//X-XSS-Protection: 1; mode=block
		//X-Frame-Options: SAMEORIGIN
	    //Expires: Mon, 20 Jun 2016 02:33:52 GMT\r\n
	    //Cache-Control: private, max-age=31536000\r\n
	    //Last-Modified: Mon, 02 Apr 2012 02:13:37 GMT\r\n
		//X-Content-Type-Options: nosniff\r\n
		
		if(connHeader == null)
			return;
		else if(!"keep-alive".equals(connHeader))
			return;

		//just re-use the connHeader from the request...
		response.addHeader(request.getHeaderLookupStruct().getHeader(Http2HeaderName.CONNECTION));
	}
	
	private List<RouterCookie> createCookies(boolean isInternalError) {
		if(!Current.isContextSet())
			return new ArrayList<>(); //in some exceptional cases like incoming cookies failing to parse, there will be no cookies
		
		try {
			List<RouterCookie> cookies = new ArrayList<>();
			cookieTranslator.addScopeToCookieIfExist(cookies, Current.flash());
			cookieTranslator.addScopeToCookieIfExist(cookies, Current.validation());
			cookieTranslator.addScopeToCookieIfExist(cookies, Current.session());
			return cookies;
		} catch(CookieTooLargeException e) {
			if(!isInternalError)
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
	
	private Http2Header create(RouterCookie c) {
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

	public void addDeleteCookie(Http2Response response, String badCookieName) {
		RouterCookie cookie = cookieTranslator.createDeleteCookie(badCookieName);
		Http2Header cookieHeader = create(cookie);
		response.addHeader(cookieHeader);
	}

}
