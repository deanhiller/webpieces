package org.webpieces.webserver.test;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.ContentType;
import org.webpieces.httpparser.api.dto.HttpChunk;
import org.webpieces.httpparser.api.dto.HttpLastChunk;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.KnownStatusCode;

/**
 * This ensures your test is the same rather it is a chunked download of the resource or a single HttpResponse so testing
 * becomes easier and changes between chunked and non-chunked coming down don't matter 
 */
public class FullResponse {

	private static final Charset DEFAULT_CHARSET = Charset.forName("ISO-8859-1");
	private HttpResponse response;
	private List<HttpChunk> chunks = new ArrayList<>();
	private HttpLastChunk lastChunk;
	
	public FullResponse(HttpResponse response) {
		this.response = response;
	}

	public void addChunk(HttpChunk httpChunk) {
		chunks.add(httpChunk);
	}

	public HttpResponse getResponse() {
		return response;
	}

	public List<HttpChunk> getChunks() {
		return chunks;
	}

	public HttpLastChunk getLastChunk() {
		return lastChunk;
	}

	public void setLastChunk(HttpLastChunk lastHttpChunk) {
		lastChunk = lastHttpChunk;
	}

	public DataWrapper getBody() {
		if(chunks.size() == 0)
			return response.getBodyNonNull();
		throw new UnsupportedOperationException("Need to implement with DataGen to chain all chunks together");
	}

	public String getBodyAsString() {
		Header header = response.getHeaderLookupStruct().getHeader(KnownHeaderName.CONTENT_TYPE);
		if(header == null)
			throw new IllegalArgumentException("no ContentType header could be found");
		ContentType ct = ContentType.parse(header);
		Charset charset = DEFAULT_CHARSET;
		if(ct.getCharSet() != null)
			charset = Charset.forName(ct.getCharSet());
		
		//get charset from headers?
		DataWrapper body = getBody();
		if(body == null)
			return null;
		return body.createStringFrom(0, body.getReadableSize(), charset);
	}

	public void assertStatusCode(KnownStatusCode status) {
		KnownStatusCode knownStatus = response.getStatusLine().getStatus().getKnownStatus();
		if(status != knownStatus)
			throw new IllegalStateException("Expected status="+status+" but received="+knownStatus);
	}

	public void assertContains(String text) {
		String bodyAsString = getBodyAsString();
		if(!bodyAsString.contains(text))
			throw new IllegalStateException("Expected body to contain='"+text+"' but body was="+bodyAsString);
	}

}
