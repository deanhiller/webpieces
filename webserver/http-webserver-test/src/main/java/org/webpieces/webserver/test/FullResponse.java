package org.webpieces.webserver.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.ContentType;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.KnownStatusCode;

/**
 * This ensures your test is the same rather it is a chunked download of the resource or a single HttpResponse so testing
 * becomes easier and changes between chunked and non-chunked coming down don't matter 
 */
public class FullResponse {

	private static final Charset DEFAULT_CHARSET = Charset.forName("ISO-8859-1");
	private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private final HttpFullResponse fullResp;
	
	public FullResponse(HttpFullResponse resp) {
		this.fullResp = resp;
	}

	public HttpResponse getResponse() {
		return fullResp.getResponse();
	}

	public DataWrapper getBody() {
		return fullResp.getData();
	}

	public String getBodyAsString() {
		Charset charset = extractCharset();
		
		//get charset from headers?
		DataWrapper body = getBody();
		if(body == null)
			return null;
		return body.createStringFrom(0, body.getReadableSize(), charset);
	}

	private Charset extractCharset() {
		Header header = getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.CONTENT_TYPE);
		if(header == null)
			throw new IllegalArgumentException("no ContentType header could be found");
		ContentType ct = ContentType.parse(header);
		Charset charset = DEFAULT_CHARSET;
		if(ct.getCharSet() != null)
			charset = Charset.forName(ct.getCharSet());
		return charset;
	}

	public void assertStatusCode(KnownStatusCode status) {
		KnownStatusCode knownStatus = getResponse().getStatusLine().getStatus().getKnownStatus();
		if(status != knownStatus)
			throw new IllegalStateException("Expected status="+status+" but received="+knownStatus);
	}

	public void assertContains(String text) {
		String bodyAsString = getBodyAsString();
		if(!bodyAsString.contains(text))
			throw new IllegalStateException("Expected body to contain='"+text+"' but body was="+bodyAsString);
	}

	public void assertNotContains(String text) {
		String bodyAsString = getBodyAsString();
		if(bodyAsString.contains(text))
			throw new IllegalStateException("Expected body to NOT contain='"+text+"' but body was="+bodyAsString);		
	}

	public void assertContentType(String mimeType) {
		Header type = getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.CONTENT_TYPE);
		String value = type.getValue();
		if(!mimeType.equals(value))
			throw new IllegalStateException("Expected mimeType="+mimeType+" but found type="+value);
	}

	public void uncompressBodyAndAssertContainsString(String text) {
		Header header = getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.CONTENT_ENCODING);
		if(header == null)
			throw new IllegalStateException("Body is not compressed as no CONTENT_ENCODING header field exists");
		else if(!"gzip".equals(header.getValue()))
			throw new IllegalStateException("Body has wrong compression type="+header.getValue()+" in CONTENT_ENCODING header field");

		DataWrapper wrapper = getBody();
		byte[] compressed = wrapper.createByteArray();
		ByteArrayInputStream in = new ByteArrayInputStream(compressed);
		byte[] out = new byte[10000];
		DataWrapper output = dataGen.emptyWrapper();
		try (GZIPInputStream str = new GZIPInputStream(in)) {
			int read = 0;
			while((read = str.read(out)) > 0) {
				ByteBuffer buffer = ByteBuffer.wrap(out, 0, read);
				DataWrapper byteWrapper = dataGen.wrapByteBuffer(buffer);
				output = dataGen.chainDataWrappers(output, byteWrapper);
				out = new byte[10000];
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		
		Charset charset = extractCharset();
		String bodyAsString = output.createStringFrom(0, output.getReadableSize(), charset);
		if(!bodyAsString.contains(text))
			throw new IllegalStateException("Expected compressed body to contain='"+text+"' but body was="+bodyAsString);
	}

	public String getRedirectUrl() {
		Header header = getResponse().getHeaderLookupStruct().getHeader(KnownHeaderName.LOCATION);
		if(header == null)
			return null;
		return header.getValue();
	}

	/**
	 * Example request cookie from chrome
	 * Cookie: webSession=1-gzvc03bKRP2YYvWySwgENREwFSg=:__ST=3a2fda5dad7547d3b15b1f61bd3d12f5; webFlash=1:_message=Invalid+values+below&user.address.zipCode=Text+instead+of+number&__secureToken=3a2fda5dad7547d3b15b1f61bd3d12f5&user.firstName=Dean+Hiller; webErrors=1:user.address.zipCode=Could+not+convert+value
	 * @return
	 */
	public Header createCookieRequestHeader() {
		List<Header> headers = getResponse().getHeaderLookupStruct().getHeaders(KnownHeaderName.SET_COOKIE);
		String fullRequestCookie = "";
		boolean firstLine = true;
		for(Header header : headers) {
			String value = header.getValue();
			if(value.contains(";")) {
				String[] split = value.split(";");
				value = split[0];
			}
			
			String[] keyVal = value.split("=");
			if(keyVal.length <= 1)
				continue; //skip adding this cookie as it was cleared out
			
			if(firstLine) {
				firstLine = false;
				fullRequestCookie += value;
			} else
				fullRequestCookie += "; "+value;
				
		}
		
		return new Header(KnownHeaderName.COOKIE, fullRequestCookie);
	}

}
