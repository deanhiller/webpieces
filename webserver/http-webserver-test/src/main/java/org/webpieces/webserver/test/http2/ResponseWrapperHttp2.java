package org.webpieces.webserver.test.http2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.http2client.api.dto.ContentType;
import org.webpieces.http2client.api.dto.FullResponse;

import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.StatusCode;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;

/**
 * This ensures your test is the same rather it is a chunked download of the resource or a single HttpResponse so testing
 * becomes easier and changes between chunked and non-chunked coming down don't matter 
 */
public class ResponseWrapperHttp2 {

	private static final Charset DEFAULT_CHARSET = Charset.forName("ISO-8859-1");
	private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private final FullResponse fullResp;
	
	public ResponseWrapperHttp2(FullResponse resp) {
		this.fullResp = resp;
	}

	public Http2Response getResponse() {
		return fullResp.getHeaders();
	}

	public DataWrapper getBody() {
		return fullResp.getPayload();
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
		Http2Header header = getResponse().getHeaderLookupStruct().getHeader(Http2HeaderName.CONTENT_TYPE);
		if(header == null)
			throw new IllegalArgumentException("no ContentType header could be found");
		ContentType ct = ContentType.parse(header);
		Charset charset = DEFAULT_CHARSET;
		if(ct.getCharSet() != null)
			charset = Charset.forName(ct.getCharSet());
		return charset;
	}

	public void assertStatusCode(StatusCode status) {
		StatusCode knownStatus = getResponse().getKnownStatusCode();
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
		Http2Header type = getResponse().getHeaderLookupStruct().getHeader(Http2HeaderName.CONTENT_TYPE);
		String value = type.getValue();
		if(!mimeType.equals(value))
			throw new IllegalStateException("Expected mimeType="+mimeType+" but found type="+value);
	}

	public void uncompressBodyAndAssertContainsString(String text) {
		Http2Header header = getResponse().getHeaderLookupStruct().getHeader(Http2HeaderName.CONTENT_ENCODING);
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
		Http2Header header = getResponse().getHeaderLookupStruct().getHeader(Http2HeaderName.LOCATION);
		if(header == null)
			return null;
		return header.getValue();
	}

	public Map<String, String> modifyCookieMap(Map<String, String> currentCookies) {
		List<Http2Header> headers = getResponse().getHeaderLookupStruct().getHeaders(Http2HeaderName.SET_COOKIE);

		for(Http2Header header : headers) {
			String value = header.getValue();
			if(value.contains(";")) {
				String[] split = value.split(";");
				value = split[0];
			}

			
			int indexOf = value.indexOf("=");
			String key = value.substring(0, indexOf);
			String val = value.substring(indexOf+1);
			
			if(val.length() <= 0) {
				currentCookies.remove(key);
			} else {
				currentCookies.put(key, val);
			}
		}
		
		return currentCookies;
	}
	
	/**
	 * Example request cookie from chrome
	 * Cookie: webSession=1-gzvc03bKRP2YYvWySwgENREwFSg=:__ST=3a2fda5dad7547d3b15b1f61bd3d12f5; webFlash=1:_message=Invalid+values+below&user.address.zipCode=Text+instead+of+number&__secureToken=3a2fda5dad7547d3b15b1f61bd3d12f5&user.firstName=Dean+Hiller; webErrors=1:user.address.zipCode=Could+not+convert+value
	 * @return
	 */
	public Http2Header createCookieRequestHeader() {
		List<Http2Header> headers = getResponse().getHeaderLookupStruct().getHeaders(Http2HeaderName.SET_COOKIE);
		String fullRequestCookie = "";
		boolean firstLine = true;
		for(Http2Header header : headers) {
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
		
		return new Http2Header(Http2HeaderName.COOKIE, fullRequestCookie);
	}

}
