package org.webpieces.httpparser.impl;

import java.nio.charset.Charset;
import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.ParseException;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.dto.HttpChunk;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpLastChunk;
import org.webpieces.httpparser.api.dto.HttpLastData;
import org.webpieces.httpparser.api.dto.HttpMessage;
import org.webpieces.httpparser.api.dto.HttpVersion;

public class ParserUtil {

	static final Charset ISO8859_1 = HttpParserFactory.ISO8859_1;

	
	public HttpLastChunk translate(HttpLastData payload) {
		HttpLastChunk chunk = new HttpLastChunk();
		chunk.setBody(payload.getBodyNonNull());
		chunk.setExtensions(payload.getExtensions());
		
		for(Header header : payload.getHeaders()) {
			chunk.addHeader(header);
		}
		return chunk;
	}

	public HttpChunk translateData(HttpData payload) {
		HttpChunk chunk = new HttpChunk(payload.getBodyNonNull());
		chunk.setExtensions(payload.getExtensions());
		return chunk;
	}
	
	public void copyData(DataWrapper body, byte[] data, int offset) {
		for(int i = 0; i < body.getReadableSize(); i++) {
			//TODO: Think about using System.arrayCopy here(what is faster?)
			data[offset + i] = body.readByteAt(i);
		}
	}

	public byte[] chunkedBytes(HttpChunk request) {
		DataWrapper dataWrapper = request.getBody();
		int size = dataWrapper.getReadableSize();

		String metaLine = request.createMetaLine();
		String lastPart = request.createTrailer();
		
		byte[] hex = metaLine.getBytes(ISO8859_1);
		byte[] endData = lastPart.getBytes(ISO8859_1);
		
		byte[] data = new byte[hex.length+size+endData.length];

		//copy chunk header of <size>/r/n
		System.arraycopy(hex, 0, data, 0, hex.length);
		
		copyData(dataWrapper, data, hex.length);

		//copy closing /r/n (and headers if isLastChunk)
		System.arraycopy(endData, 0, data, data.length-endData.length, endData.length);
		
		return data;
	}
	
	public HttpVersion parseVersion(String versionString, String firstLine) {
		if(!versionString.startsWith("HTTP/")) {
			throw new ParseException("Invalid version in http request first line not prefixed with HTTP/.  line="+firstLine);
		}
		
		String ver = versionString.substring(5, versionString.length());
		HttpVersion version = new HttpVersion();
		version.setVersion(ver);
		return version;
	}

	public void parseHeaders(List<String> lines, HttpMessage httpMessage) {
		//TODO: one header can be multiline and we need to fix this code for that
		//ie. the spec says you can split a head in multiple lines(ick!!!)
		for(String line : lines) {
			Header header = parseHeader(line);
			httpMessage.addHeader(header);
		}
	}

	public Header parseHeader(String line) {
		//can't use split in case there are two ':' ...one in the value and one as the delimeter
		int indexOf = line.indexOf(":");
		if(indexOf < 0)
			throw new IllegalArgumentException("bad header line="+ line);
		String value = line.substring(indexOf+1).trim();
		String name = line.substring(0, indexOf);
		Header header = new Header();
		header.setName(name.trim());
		header.setValue(value.trim());
		return header;
	}
}
