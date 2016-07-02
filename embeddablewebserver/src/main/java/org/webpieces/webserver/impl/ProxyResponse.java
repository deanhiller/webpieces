package org.webpieces.webserver.impl;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

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
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.HttpResponseStatus;
import org.webpieces.httpparser.api.dto.HttpResponseStatusLine;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.actions.RenderHtml;
import org.webpieces.router.api.dto.RedirectResponse;
import org.webpieces.router.api.dto.RenderResponse;
import org.webpieces.router.api.dto.View;
import org.webpieces.router.api.exceptions.IllegalReturnValueException;
import org.webpieces.templating.api.Template;
import org.webpieces.templating.api.TemplateService;

public class ProxyResponse implements ResponseStreamer {

	private static final Logger log = LoggerFactory.getLogger(ProxyResponse.class);
	private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("E, dd MMM Y HH:mm:ss");
	private static final DataWrapperGenerator wrapperFactory = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private FrontendSocket channel;
	private HttpRequest request;
	private TemplateService templatingService;

	public ProxyResponse(HttpRequest req, FrontendSocket channel, TemplateService templatingService) {
		this.request = req;
		this.channel = channel;
		this.templatingService = templatingService;
	}

	@Override
	public void sendRedirect(RedirectResponse httpResponse) {
		HttpResponseStatus status = new HttpResponseStatus();
		status.setKnownStatus(KnownStatusCode.HTTP303);
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
		
		addCommonHeaders(response);
		
		log.info("sending response channel="+channel);
		channel.write(response);

		closeIfNeeded();
	}

	@Override
	public void sendRenderHtml(RenderResponse resp) {
		HttpResponseStatus status = new HttpResponseStatus();
		if(resp.isNotFoundRoute())
			status.setKnownStatus(KnownStatusCode.HTTP404);
		else
			status.setKnownStatus(KnownStatusCode.HTTP200);
		
		HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
		statusLine.setStatus(status);
		HttpResponse response = new HttpResponse();
		response.setStatusLine(statusLine);

		View view = resp.getView();
		String packageStr = view.getControllerPackage();
		//For this type of View, the template is the name of the method..
		String templateClassName = view.getMethodName();
		
		Map<String, Object> args = new HashMap<>();
		Template template = templatingService.loadTemplate(packageStr, templateClassName, "html");
		
		//stream this out with chunked response instead....
		StringWriter out = new StringWriter();
		template.run(args, out);
		
		String content = out.toString();
		byte[] bytes = content.getBytes();
		
		Header header = new Header(KnownHeaderName.CONTENT_LENGTH, bytes.length+"");
		response.addHeader(header);
		
		DataWrapper data = wrapperFactory.wrapByteArray(bytes);
		response.setBody(data);

		addCommonHeaders(response);
		
		log.info("sending response channel="+channel);
		channel.write(response);
		
		closeIfNeeded();
	}
	
	private void addCommonHeaders(HttpResponse response) {
		Header connHeader = request.getHeaderLookupStruct().getHeader(KnownHeaderName.CONNECTION);
		
		DateTime now = DateTime.now().toDateTime(DateTimeZone.UTC);
		String dateStr = formatter.print(now)+" GMT";

		//in general, nearly all these headers are desired..
		log.error("Add these headers to Frontend to be re-used via a headerAdd plugin which is added by default but can be removed for users not wanting it");
		Header date = new Header(KnownHeaderName.DATE, dateStr);
		response.addHeader(date);

		Header xFrame = new Header("X-Frame-Options", "SAMEORIGIN");
		response.addHeader(xFrame);
		
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

		//TODO: Should really move this into the Frontend to warn clients of timeout values before
		//we close their connection
		Header keepAlive = new Header(KnownHeaderName.KEEP_ALIVE, "timeout=3, max=100");
		//just re-use the connHeader from the request...
		response.addHeader(connHeader);
		response.addHeader(keepAlive);
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
	public void failure(Throwable e) {
		log.error("Exception", e);
	}

}
