package org.webpieces.webserver.impl;

import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend.api.FrontendSocket;
import org.webpieces.frontend.api.exception.HttpException;
import org.webpieces.httpparser.api.common.Cookie;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.HttpResponseStatus;
import org.webpieces.httpparser.api.dto.HttpResponseStatusLine;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.dto.RedirectResponse;
import org.webpieces.router.api.dto.RenderResponse;
import org.webpieces.router.api.dto.RouterCookie;
import org.webpieces.router.api.dto.View;
import org.webpieces.router.api.exceptions.IllegalReturnValueException;
import org.webpieces.templating.api.ReverseUrlLookup;
import org.webpieces.templating.api.Template;
import org.webpieces.templating.api.TemplateService;
import org.webpieces.templating.api.TemplateUtil;
import org.webpieces.webserver.api.WebServerConfig;

import groovy.lang.MissingPropertyException;

public class ProxyResponse implements ResponseStreamer {

	private static final Logger log = LoggerFactory.getLogger(ProxyResponse.class);
	private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("E, dd MMM Y HH:mm:ss");
	private static final DataWrapperGenerator wrapperFactory = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private FrontendSocket channel;
	private HttpRequest request;
	private TemplateService templatingService;
	private WebServerConfig config;
	private ReverseUrlLookup lookup;

	public ProxyResponse(HttpRequest req, FrontendSocket channel, ReverseUrlLookup lookup, TemplateService templatingService, WebServerConfig config) {
		this.request = req;
		this.channel = channel;
		this.lookup = lookup;
		this.templatingService = templatingService;
		this.config = config;
	}

	public ProxyResponse(FrontendSocket channel) {
		this.channel = channel;
	}
	
	@Override
	public void sendRedirect(RedirectResponse httpResponse) {
		HttpResponseStatus status = new HttpResponseStatus();
		status.setKnownStatus(KnownStatusCode.HTTP_303_SEEOTHER);
		HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
		statusLine.setStatus(status);
		HttpResponse response = new HttpResponse();
		response.setStatusLine(statusLine);
		
		String url = httpResponse.redirectToPath;
		
		if(httpResponse.domain != null && httpResponse.isHttps != null) {
			String prefix = "http://";
			if(httpResponse.isHttps)
				prefix = "https://";
			url = prefix + httpResponse.domain + httpResponse.redirectToPath;
		} else if(httpResponse.domain != null) {
			throw new IllegalReturnValueException("Controller is returning a domain without returning isHttps=true or"
					+ " isHttps=false so we can form the entire redirect.  Either drop the domain or set isHttps");
		} else if(httpResponse.isHttps != null) {
			throw new IllegalReturnValueException("Controller is returning isHttps="+httpResponse.isHttps+" but there is"
					+ "no domain set so we can't form the full redirect.  Either drop setting isHttps or set the domain");
		}
		
		Header location = new Header(KnownHeaderName.LOCATION, url);
		response.addHeader(location );
		
		//Firefox requires a content length of 0 (chrome doesn't)!!!...
		addCommonHeaders(response, 0, httpResponse.cookies);
		
		log.info("sending REDIRECT response channel="+channel);
		channel.write(response);

		closeIfNeeded();
	}

	@Override
	public void sendRenderHtml(RenderResponse resp) {
		View view = resp.view;
		String packageStr = view.getControllerPackage();
		//For this type of View, the template is the name of the method..
		String templateClassName = view.getMethodName();
		
		String path = getTemplatePath(packageStr, templateClassName, "html");
		
		//TODO: get html from the request such that we look up the correct template? AND if not found like they request only json, than
		//we send back a 404 rather than a 500
		Template template = templatingService.loadTemplate(path);

		//TODO: stream this out with chunked response instead??....
		StringWriter out = new StringWriter();
		
		try {
			templatingService.runTemplate(template, out, resp.pageArgs, lookup);
		} catch(MissingPropertyException e) {
			Set<String> keys = resp.pageArgs.keySet();
			throw new ControllerPageArgsException("Controller.method="+view.getControllerName()+"."+view.getMethodName()+" did\nnot"
					+ " return enough arguments for the template.  specifically, the method\nreturned these"
					+ " arguments="+keys+"  There is a chance in your html you forgot the '' around a variable name\n"
							+ "such as #{set 'key'}# but you put #{set key}# which is 'usually' not the correct way\n"
							+ "The missing properties are as follows....\n"+e.getMessage(), e);
		}
		
		String content = out.toString();
		
		KnownStatusCode statusCode = KnownStatusCode.HTTP_200_OK;
		switch(resp.routeType) {
		case BASIC:
			statusCode = KnownStatusCode.HTTP_200_OK;
			break;
		case NOT_FOUND:
			statusCode = KnownStatusCode.HTTP_404_NOTFOUND;
			break;
		case INTERNAL_SERVER_ERROR:
			statusCode = KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR;
			break;
		default:
			throw new IllegalStateException("did add case for state="+resp.routeType);
		}
		
		HttpResponse response = createResponse(statusCode, content, resp.cookies);
		
		log.info("sending RENDERHTML response. code="+statusCode+" for path="+request.getRequestLine().getUri().getUri()+" channel="+channel);
		if(log.isDebugEnabled())
			log.debug("content sent back="+content);
		
		channel.write(response);
		
		closeIfNeeded();
	}

	private String getTemplatePath(String packageStr, String templateClassName, String extension) {
		String className = templateClassName;
		if(!"".equals(packageStr))
			className = packageStr+"."+className;
		if(!"".equals(extension))
			className = className+"_"+extension;
		
		return TemplateUtil.convertTemplateClassToPath(className);
	}

	private HttpResponse createResponse(KnownStatusCode statusCode, String content, List<RouterCookie> cookies) {
		Charset encoding = config.getHtmlResponsePayloadEncoding();

		byte[] bytes = content.getBytes(encoding);
		
		HttpResponseStatus status = new HttpResponseStatus();
		status.setKnownStatus(statusCode);
		
		HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
		statusLine.setStatus(status);
		HttpResponse response = new HttpResponse();
		response.setStatusLine(statusLine);
		
		Header contentType = new Header(KnownHeaderName.CONTENT_TYPE, "text/html; charset="+encoding.name().toLowerCase());
		response.addHeader(contentType);
		
		DataWrapper data = wrapperFactory.wrapByteArray(bytes);
		response.setBody(data);

		addCommonHeaders(response, bytes.length, cookies);
		return response;
	}
	
	private void addCommonHeaders(HttpResponse response, int contentLength, List<RouterCookie> cookies) {
		
		Header header = new Header(KnownHeaderName.CONTENT_LENGTH, contentLength+"");
		response.addHeader(header);
		
		Header connHeader = request.getHeaderLookupStruct().getHeader(KnownHeaderName.CONNECTION);
		
		DateTime now = DateTime.now().toDateTime(DateTimeZone.UTC);
		String dateStr = formatter.print(now)+" GMT";

		//in general, nearly all these headers are desired..
		Header date = new Header(KnownHeaderName.DATE, dateStr);
		response.addHeader(date);

//		Header xFrame = new Header("X-Frame-Options", "SAMEORIGIN");
//		response.addHeader(xFrame);
		
		for(RouterCookie c : cookies) {
			Header cookieHeader = create(c);
			response.addHeader(cookieHeader);
		}
		
		//X-XSS-Protection: 1; mode=block
		//X-Frame-Options: SAMEORIGIN
		//Content-Type: image/gif\r\n
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

	private Header create(RouterCookie c) {
		Cookie cookie = new Cookie();
		cookie.setName(c.name);
		cookie.setValue(c.value);
		cookie.setDomain(c.domain);
		cookie.setPath(c.path);
		cookie.setMaxAgeSeconds(c.maxAgeSeconds);
		cookie.setSecure(c.isSecure);
		cookie.setHttpOnly(c.isHttpOnly);
		return cookie.createHeader();
	}

	private void closeIfNeeded() {
		Header connHeader = request.getHeaderLookupStruct().getHeader(KnownHeaderName.CONNECTION);
		boolean close = false;
		if(connHeader != null) {
			String value = connHeader.getValue();
			if(!"keep-alive".equals(value)) {
				close = true;
			}
		} else
			close = true;
		
		if(close)
			channel.close();
	}

	@Override
	public void failureRenderingInternalServerErrorPage(Throwable e) {
		//This is a final failure so we send a webpieces page next (in the future, we should just use a customer static html file if set)
		//This is only if the webapp 500 html page fails as many times it is a template and they could have another bug in that template.
		String html = "<html><head></head><body>This website had a bug, "
				+ "then when rendering the page explaining the bug, well, they hit another bug.  "
				+ "The webpieces platform saved them from sending back an ugly stack trace.  Contact website owner "
				+ "with a screen shot of this page</body></html>";
		HttpResponse response = createResponse(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR, html, new ArrayList<>());
		
		channel.write(response);
		
		closeIfNeeded();
	}

	public void sendFailure(HttpException exc) {
		HttpResponseStatus status = new HttpResponseStatus();
		status.setKnownStatus(exc.getStatusCode());
		
		HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
		statusLine.setStatus(status);
		HttpResponse response = new HttpResponse();
		response.setStatusLine(statusLine);
		
		response.addHeader(new Header("Failure", exc.getMessage()));
		
		channel.write(response);
	}

}
