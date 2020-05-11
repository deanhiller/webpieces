package org.webpieces.router.impl.proxyout;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.Constants;
import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RouterCookie;
import org.webpieces.router.api.exceptions.CookieTooLargeException;
import org.webpieces.router.api.exceptions.IllegalReturnValueException;
import org.webpieces.router.impl.CookieTranslator;
import org.webpieces.router.impl.compression.MimeTypes;
import org.webpieces.router.impl.compression.MimeTypes.MimeTypeResult;
import org.webpieces.router.impl.dto.RedirectResponse;

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

	private final CookieTranslator cookieTranslator;
	private final MimeTypes mimeTypes;
	private final String version;

	@Inject
	public ResponseCreator(CookieTranslator cookieTranslator, MimeTypes mimeTypes) {
		version = "webpieces/"+readVersion();
		this.cookieTranslator = cookieTranslator;
		this.mimeTypes = mimeTypes;
	}

	public String readVersion() {
		Properties properties = new Properties();
		try (InputStream stream = this.getClass().getResourceAsStream("/webpiecesVersion.properties")) {
			properties.load(stream);
			return properties.getProperty("version");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

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

		Http2Response response = addCommonHeaders(request, mimeType.mime, isDynamicPartOfWebsite, statusCode, reason);

		response.setEndOfStream(false);

		response.addHeader(new Http2Header("reason", reason));

		return new ResponseEncodingTuple(response, mimeType);
	}

	public Http2Response createRedirect(Http2Request request, RedirectResponse httpResponse) {
		Http2Response response;

		if(httpResponse.isAjaxRedirect) {
			response = addCommonHeaders(request, null, true, 
					Constants.AJAX_REDIRECT_CODE, "Ajax Redirect");
		} else {
			response = addCommonHeaders(request, null, true, 
					StatusCode.HTTP_303_SEEOTHER.getCode(), StatusCode.HTTP_303_SEEOTHER.getReason());
		}

		String url = httpResponse.redirectToPath;
		if(url.startsWith("http")) {
			//do nothing
		} else if(httpResponse.domain != null && httpResponse.isHttps != null) {
			String prefix = "http://";
			if(httpResponse.isHttps)
				prefix = "https://";

			String portPostfix = "";
			if(httpResponse.port != 443 && httpResponse.port != 80)
				portPostfix = ":"+httpResponse.port;

			url = prefix + httpResponse.domain + portPostfix + httpResponse.redirectToPath;
		} else if(httpResponse.domain != null) {
			throw new IllegalReturnValueException("Controller is returning a domain without returning isHttps=true or"
					+ " isHttps=false so we can form the entire redirect.  Either drop the domain or set isHttps");
		} else if(httpResponse.isHttps != null) {
			throw new IllegalReturnValueException("Controller is returning isHttps="+httpResponse.isHttps+" but there is"
					+ "no domain set so we can't form the full redirect.  Either drop setting isHttps or set the domain");
		}

		Http2Header location = new Http2Header(Http2HeaderName.LOCATION, url);
		response.addHeader(location );

		//Firefox requires a content length of 0 on redirect(chrome doesn't)!!!...
		response.addHeader(new Http2Header(Http2HeaderName.CONTENT_LENGTH, 0+""));
		return response;
	}

    public static class ResponseEncodingTuple {
		public Http2Response response;
		public MimeTypeResult mimeType;

		public ResponseEncodingTuple(Http2Response response, MimeTypeResult mimeType) {
			this.response = response;
			this.mimeType = mimeType;
		}	
	}

	public Http2Response addCommonHeaders(Http2Request request, String responseMimeType, boolean isDynamicPartOfWebsite, int statusCode, String statusReason) {
		Http2Response response = addCommonHeaders2(request, responseMimeType, statusCode, statusReason);

		boolean isInternalError = statusCode >= 500 && statusCode < 600;
		
		if(isDynamicPartOfWebsite) {
			List<RouterCookie> cookies = createCookies(isInternalError);
			for(RouterCookie c : cookies) {
				Http2Header cookieHeader = create(c);
				response.addHeader(cookieHeader);
			}
		}
		
		return response;
	}
	
	public Http2Response addCommonHeaders2(Http2Request request, String responseMimeType, int statusCode, String statusReason) {
		Http2Response response = new Http2Response();
		
		response.addHeader(new Http2Header(Http2HeaderName.STATUS, statusCode+""));
		if(statusReason != null)
			response.addHeader(new Http2Header("reason", statusReason));
		
		String connHeader = request.getSingleHeaderValue(Http2HeaderName.CONNECTION);
		
		DateTime now = DateTime.now().toDateTime(DateTimeZone.UTC);
		String dateStr = formatter.print(now)+" GMT";


		Http2Header versionHeader = new Http2Header(Http2HeaderName.SERVER, version);
		response.addHeader(versionHeader);

		//in general, nearly all these headers are desired..
		Http2Header date = new Http2Header(Http2HeaderName.DATE, dateStr);
		response.addHeader(date);

		if(responseMimeType != null)
			response.addHeader(new Http2Header(Http2HeaderName.CONTENT_TYPE, responseMimeType));

//		Header xFrame = new Header("X-Frame-Options", "SAMEORIGIN");
//		response.addHeader(xFrame);
		
		//X-XSS-Protection: 1; mode=block
		//X-Frame-Options: SAMEORIGIN
	    //Expires: Mon, 20 Jun 2016 02:33:52 GMT\r\n
	    //Cache-Control: private, max-age=31536000\r\n
	    //Last-Modified: Mon, 02 Apr 2012 02:13:37 GMT\r\n
		//X-Content-Type-Options: nosniff\r\n
		
		if(connHeader == null)
			return response;
		else if(!"keep-alive".equals(connHeader))
			return response;

		//just re-use the connHeader from the request...
		response.addHeader(request.getHeaderLookupStruct().getHeader(Http2HeaderName.CONNECTION));
		return response;
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
