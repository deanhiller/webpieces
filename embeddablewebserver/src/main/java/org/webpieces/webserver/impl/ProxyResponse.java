package org.webpieces.webserver.impl;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
import org.webpieces.router.api.dto.RedirectResponse;
import org.webpieces.router.api.dto.RenderResponse;

public class ProxyResponse implements ResponseStreamer {

	private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("E, dd MMM Y HH:mm:ss");
	private static final DataWrapperGenerator wrapperFactory = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private FrontendSocket channel;
	private HttpRequest request;

	public ProxyResponse(HttpRequest req, FrontendSocket channel) {
		this.request = req;
		this.channel = channel;
	}

	@Override
	public void sendRedirect(RedirectResponse httpResponse) {
		HttpResponseStatus status = new HttpResponseStatus();
		status.setKnownStatus(KnownStatusCode.HTTP303);
		HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
		statusLine.setStatus(status);
		HttpResponse response = new HttpResponse();
		response.setStatusLine(statusLine);
		
		String prefix = "http";
		if(httpResponse.isHttps)
			prefix = "https";
		
		String url = prefix + "://" + httpResponse.domain + httpResponse.redirectToPath;
		Header location = new Header(KnownHeaderName.LOCATION, url);
		response.addHeader(location );
		
		DateTime now = DateTime.now().toDateTime(DateTimeZone.UTC);
		String dateStr = formatter.print(now)+" GMT";
		
		Header date = new Header(KnownHeaderName.DATE, dateStr);
		response.addHeader(date);
		
		channel.write(response);

		closeIfNeeded();
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
	public void sendRenderHtml(RenderResponse resp) {
		HttpResponseStatus status = new HttpResponseStatus();
		status.setKnownStatus(KnownStatusCode.HTTP200);
		HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
		statusLine.setStatus(status);
		HttpResponse response = new HttpResponse();
		response.setStatusLine(statusLine);

		String content = "<html><head></head><body>Hello World</body></html>";
		byte[] bytes = content.getBytes();
		
		Header header = new Header(KnownHeaderName.CONTENT_LENGTH, bytes.length+"");
		response.addHeader(header);
		
		DataWrapper data = wrapperFactory.wrapByteArray(bytes);
		response.setBody(data);

		channel.write(response);
		
	}

	@Override
	public void failure(Throwable e) {
	}

}
