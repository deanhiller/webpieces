package org.webpieces.httpparser.impl;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.httpparser.api.ParseException;
import org.webpieces.httpparser.api.ParsedStatus;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpChunk;
import org.webpieces.httpparser.api.dto.HttpChunkExtension;
import org.webpieces.httpparser.api.dto.HttpLastChunk;
import org.webpieces.httpparser.api.dto.HttpMessage;
import org.webpieces.httpparser.api.dto.HttpMessageType;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.HttpRequestMethod;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.HttpResponseStatus;
import org.webpieces.httpparser.api.dto.HttpResponseStatusLine;
import org.webpieces.httpparser.api.dto.HttpUri;
import org.webpieces.httpparser.api.dto.HttpVersion;

public class HttpParserImpl implements HttpParser {

	//private static final Logger log = LoggerFactory.getLogger(HttpParserImpl.class);
	private static final Charset iso8859_1 = Charset.forName("ISO-8859-1");
	private static final String TRAILER_STR = "\r\n";
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private static final DataWrapper EMPTY_WRAPPER = dataGen.emptyWrapper();
	
	private ConvertAscii conversion = new ConvertAscii();
	private BufferPool pool;

	
	public HttpParserImpl(BufferPool pool) {
		this.pool = pool;
	}
	
	@Override
	public ByteBuffer marshalToByteBuffer(HttpPayload request) {
		//modify later to go from String straight to ByteBuffer instead...
		byte[] data = marshalToBytes(request);
		ByteBuffer buffer = ByteBuffer.wrap(data);
		return buffer;
	}
	
	@Override
	public byte[] marshalToBytes(HttpPayload request) {
		if(request.getMessageType() == HttpMessageType.CHUNK || request.getMessageType() == HttpMessageType.LAST_CHUNK) {
			return chunkedBytes((HttpChunk)request);
		}
		
		HttpMessage msg = (HttpMessage) request;
		String result = marshalHeaders(request);
		
		DataWrapper body = request.getBody();
		Header header = msg.getHeaderLookupStruct().getHeader(KnownHeaderName.CONTENT_LENGTH);
		if(header != null) {
			if(body == null)
				throw new IllegalArgumentException("Header KnownHeaderName.CONTENT_LENGTH found but no body was set.  set a body");
			String value = header.getValue();
			int lengthOfBodyFromHeader = toInteger(value, ""+header);
			int actualBodyLength = body.getReadableSize();
			if(lengthOfBodyFromHeader != actualBodyLength) {
				throw new IllegalArgumentException("body size and KnownHeaderName.CONTENT_LENGTH"
						+ " must match.  bodySize="+actualBodyLength+" header len="+lengthOfBodyFromHeader);
			}
		} else if(body != null) {
			throw new IllegalArgumentException("Body provided but no header for KnownHeaderName.CONTENT_LENGTH found");
		} else 
			body = EMPTY_WRAPPER;

		byte[] data = new byte[result.length()+body.getReadableSize()];
		
		//TODO: Is there a way to write a String to first part of byte[] rather than having to copy
		//the byte array from the string into the full byte array
		byte[] stringPiece = result.getBytes(iso8859_1);
		System.arraycopy(stringPiece, 0, data, 0, stringPiece.length);
		
		copyData(body, data, stringPiece.length);
		
		return data;
	}

	private void copyData(DataWrapper body, byte[] data, int offset) {
		for(int i = 0; i < body.getReadableSize(); i++) {
			//TODO: Think about using System.arrayCopy here(what is faster?)
			data[offset + i] = body.readByteAt(i);
		}
	}

	private byte[] chunkedBytes(HttpChunk request) {
		DataWrapper dataWrapper = request.getBody();
		int size = dataWrapper.getReadableSize();

		String metaLine = request.createMetaLine();
		String lastPart = request.createTrailer();
		
		byte[] hex = metaLine.getBytes(iso8859_1);
		byte[] endData = lastPart.getBytes(iso8859_1);
		
		byte[] data = new byte[hex.length+size+endData.length];

		//copy chunk header of <size>/r/n
		System.arraycopy(hex, 0, data, 0, hex.length);
		
		copyData(dataWrapper, data, hex.length);

		//copy closing /r/n (and headers if isLastChunk)
		System.arraycopy(endData, 0, data, data.length-endData.length, endData.length);
		
		return data;
	}

	private Integer toInteger(String value, String line) {
		try {
			return Integer.valueOf(value);
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("HttpMessage contains illegal line(could not convert value to Integer)="+line);
		}
	}

	@Override
	public String marshalToString(HttpPayload httpMsg) {
		//TODO: We could check Content-Type header and if text type, we could marshall it still?
		if(httpMsg.getMessageType() != HttpMessageType.LAST_CHUNK && httpMsg.getBody() != null)
			throw new IllegalArgumentException("Cannot marshal http message with a body to a string");
		
		return marshalHeaders(httpMsg);
	}

	private String marshalHeaders(HttpPayload httpMsg) {
		if(httpMsg.getMessageType() == HttpMessageType.REQUEST)
			validate(httpMsg.getHttpRequest());
		else if(httpMsg.getMessageType() == HttpMessageType.RESPONSE)
			validate(httpMsg.getHttpResponse());
		
		//TODO: perhaps optimize and use StringBuilder on the Header for loop
		//Java optimizes most to StringBuilder but for a for loop, it doesn't all the time...
		StringBuilder builder = new StringBuilder();
		builder.append(httpMsg + "");
		return builder.toString();
	}

	private void validate(HttpResponse response) {
		HttpResponseStatusLine statusLine = response.getStatusLine();
		if(statusLine == null) {
			throw new IllegalArgumentException("response.statusLine is not set(call response.setStatusLine");
		}
		HttpResponseStatus status = statusLine.getStatus();
		if(status == null) {
			throw new IllegalArgumentException("response.statusLine.status is not set(call response.getStatusLine().setStatus())");
		} else if(status.getCode() == null) {
			throw new IllegalArgumentException("response.statusLine.status.code is not set(call response.getStatusLine().getStatus().setCode())");
		} else if(status.getReason() == null) {
			throw new IllegalArgumentException("response.statusLine.status.reason is not set");
		} else if(statusLine.getVersion() == null) {
			throw new IllegalArgumentException("response.statusLine.version is not set");
		}
	}

	private void validate(HttpRequest request) {
		HttpRequestLine requestLine = request.getRequestLine();
		if(requestLine == null) {
			throw new IllegalArgumentException("request.requestLine is not set(call request.setRequestLine()");
		} else if(requestLine.getMethod() == null) {
			throw new IllegalArgumentException("request.requestLine.method is not set(call request.getRequestLine().setMethod()");
		} else if(requestLine.getVersion() == null) {
			throw new IllegalArgumentException("request.requestLine.version is not set(call request.getRequestLine().setVersion()");
		}
	}

	public Memento prepareToParse() {
		MementoImpl memento = new MementoImpl();
		memento.setLeftOverData(dataGen.emptyWrapper());
		return memento;
	}
	
	@Override
	public Memento parse(Memento state, DataWrapper moreData) {
		if(!(state instanceof MementoImpl)) {
			throw new IllegalArgumentException("You must always pass in the "
					+ "memento created in prepareToParse which we always hand back"
					+ "to you from this method.  It contains state of leftover data");
		}
		
//		if(log.isDebugEnabled()) {
//			byte[] someData = moreData.createByteArray();
//			String readable = conversion.convertToReadableForm(someData);
//			log.info("about to parse=\n\n'"+readable+"'\n\n");
//		}

		MementoImpl memento = (MementoImpl) state;
		//initialize state to need more data
		memento.setStatus(ParsedStatus.NEED_MORE_DATA);
		memento.getParsedMessages().clear();
		
		DataWrapper leftOverData = memento.getLeftOverData();
		DataWrapper	allData = dataGen.chainDataWrappers(leftOverData, moreData);
		memento.setLeftOverData(allData);
		
		if(memento.isInChunkParsingMode()) {
			processChunks(memento);
		} else if(memento.getHalfParsedMessage() != null) {
			readInBody(memento, false);
		}

		if(memento.getHalfParsedMessage() != null)
			return memento;  //we are still reading in the body of a chunk
		
		//This is a bit tricky but memento.getReadingHttpMessagePoint will cause this method to 
		//return immediately if we are in the middle of processChunks or readInBody
		//BUT this is here because AFTER processChunks or readInBody is complete, it should process the next
		//response as well!!!
		findCrLnCrLnAndParseMessage(memento);
		return memento;
	}

	private void findCrLnCrLnAndParseMessage(MementoImpl memento) {
//		DataWrapper leftOverData2 = memento.getLeftOverData();
//		String msg = leftOverData2.createStringFrom(0, leftOverData2.getReadableSize(), Charset.defaultCharset());
//		System.out.println("msg="+msg);
		
		//We are looking for the \r\n\r\n  (or \n\n from bad systems) to
		//discover entire payload
		int i = memento.getReadingHttpMessagePointer();
		for(; i < memento.getLeftOverData().getReadableSize() - 3; i++) {
			boolean parsedAndSplitBuffer = processUntilRead(memento, i);
			if(parsedAndSplitBuffer) {
				//reset the index for reading
				i = 0;
			}
			
			
			//do not continue if we are reading the body...
			if(memento.getHalfParsedMessage() != null) {
				break;
			}
		}
		memento.setReadingHttpMessagePointer(i);
		
		DataWrapper leftOverData = memento.getLeftOverData();
		if(leftOverData.getReadableSize() == 0) {
			memento.setStatus(ParsedStatus.ALL_DATA_PARSED);
		} else if(memento.getParsedMessages().size() > 0) {
			memento.setStatus(ParsedStatus.MSG_PARSED_AND_LEFTOVER_DATA);
		}
	}

	/**
	 * Returns true if we split the buffers up or else false
	 */
	private boolean processUntilRead(MementoImpl memento, int i) {
		DataWrapper dataToRead = memento.getLeftOverData();
		byte firstByte = dataToRead.readByteAt(i);
		byte secondByte = dataToRead.readByteAt(i+1);
		byte thirdByte = dataToRead.readByteAt(i+2);
		byte fourthByte = dataToRead.readByteAt(i+3);
		
		//For debugging to see the 4 bytes that we are processing easier
//		byte[] data = dataToRead.createByteArray();
//		String fourBytesAre = conversion.convertToReadableForm(data, i, 4);
		
		boolean isFirstCr = conversion.isCarriageReturn(firstByte);
		boolean isSecondLineFeed = conversion.isLineFeed(secondByte);
		boolean isThirdCr = conversion.isCarriageReturn(thirdByte);
		boolean isFourthLineField = conversion.isLineFeed(fourthByte);
		
		if(isFirstCr && isSecondLineFeed && isThirdCr && isFourthLineField) {
			//Found end of http headers...
			processHttpMessageAndMaybeBody(memento, dataToRead, i);
			memento.setReadingHttpMessagePointer(0);
			return true;
		}
		
		//mark any positions for \r\n
		if(isFirstCr && isSecondLineFeed) {
			memento.addDemarcation(i);
		}		
		return false;
	}

	private void processHttpMessageAndMaybeBody(MementoImpl memento, DataWrapper dataToRead, int i) {
		//A FEW ways to go here with http body....  I could 
		//1. pass back an http message while body is being filled in
		//    a. have client call getBody and block until filled or
		//    b. have client call getInputStream and block on inputStream.read
		//    c. have client call addBodyCallback(callback) such that client would unblock and
		//           we call into client with bytes, more bytes, more bytes
		//2. only pass back http message once full body is in place
		// 
		//NOTE: because of http body content and encodings, only the full body is zipped so
		//need full body anyways to unzip.  (ie. chunking is used instead of body for streaming
		//so for now, just read in entire body before returning it to the client
		
		List<Integer> markedPositions = memento.getLeftOverMarkedPositions();
		memento.setLeftOverMarkedPositions(new ArrayList<Integer>());
		List<? extends DataWrapper> tuple = dataGen.split(dataToRead, i+4);
		DataWrapper toBeParsed = tuple.get(0);
		memento.setLeftOverData(tuple.get(1));
		HttpMessage message = parseHttpMessage(toBeParsed, markedPositions);
		
		Header header = message.getHeaderLookupStruct().getHeader(KnownHeaderName.CONTENT_LENGTH);
		Header transferHeader = message.getHeaderLookupStruct().getLastInstanceOfHeader(KnownHeaderName.TRANSFER_ENCODING);
		if(header != null) {
			String value = header.getValue();
			int length = toInteger(value, ""+header);
			memento.setNumBytesLeftToRead(length);
			memento.setHalfParsedMessage(message);
			readInBody(memento, false);
			return;
		} else if(transferHeader != null && "chunked".equals(transferHeader.getValue())) {
			memento.getParsedMessages().add(message);
			memento.setInChunkParsingMode(true);
			processChunks(memento);
			return;
		}
		
		//no body in the bytestream so add the message to list of parsed messages
		memento.getParsedMessages().add(message);
	}

	private void processChunks(MementoImpl memento) {
		if(memento.getHalfParsedMessage() != null) {
			readInBody(memento, true);
			if(memento.getHalfParsedMessage() != null)
				return; //we are still reading in the body
		}
		
		
		int i = memento.getReadingHttpMessagePointer();
		for(;i < memento.getLeftOverData().getReadableSize() - 1; i++) 
		{
			DataWrapper dataToRead = memento.getLeftOverData();
			byte firstByte = dataToRead.readByteAt(i);
			byte secondByte = dataToRead.readByteAt(i+1);

			boolean isFirstCr = conversion.isCarriageReturn(firstByte);
			boolean isSecondLineFeed = conversion.isLineFeed(secondByte);
			
			if(isFirstCr && isSecondLineFeed) {
				readChunk(memento, i);
				//since we swapped out memento.getLeftOverData to be 
				//what's left, we can read from 0 again
				i = 0;
				if(!memento.isInChunkParsingMode() //we are done processing chunks
					|| memento.getHalfParsedMessage() != null) //we are in the middle of processing chunk body 	
					break; 
			}
		}
		memento.setReadingHttpMessagePointer(i);
	}

	private void readChunk(MementoImpl memento, int i) {
		HttpChunk chunk = createHttpChunk(memento, i);
		memento.setHalfParsedMessage(chunk);
		readInBody(memento, true);
		
		if(chunk.getBody() != null && chunk.getBody().getReadableSize() == 0) {
			//this is the last chunk as it is 0 size
			memento.setInChunkParsingMode(false);
		}
	}

	private HttpChunk createHttpChunk(MementoImpl memento, int i) {
		DataWrapper dataToRead = memento.getLeftOverData();
		//split off the header AND /r/n (ie. the +2)
		List<? extends DataWrapper> split = dataGen.split(dataToRead, i+2);
		DataWrapper chunkMetaData = split.get(0);
		memento.setLeftOverData(split.get(1));
		
		List<HttpChunkExtension> extensions = new ArrayList<>();
		
		String chunkMetaStr = chunkMetaData.createStringFrom(0, chunkMetaData.getReadableSize(), iso8859_1);
		String hexSize = chunkMetaStr.trim();
		if(chunkMetaStr.contains(";")) {
			String[] extensionsArray = chunkMetaStr.split(";");
			hexSize = extensionsArray[0];
			for(int n = 1; n < extensionsArray.length; n++) {
				HttpChunkExtension ext = createExtension(extensionsArray[n]);
				extensions.add(ext);
				//extensions.addExtension(ext);
			}
		}

		int chunkSize = Integer.parseInt(hexSize, 16);
		HttpChunk chunk = new HttpChunk();
		if(chunkSize == 0)
			chunk = new HttpLastChunk();
		
		//must read in all the data of the chunk AND /r/n
		int size = 2 + chunkSize;
		memento.setNumBytesLeftToRead(size);
		
		return chunk;
	}

	private HttpChunkExtension createExtension(String extension) {
		if(!extension.contains("=")) {
			return new HttpChunkExtension(extension);
		}
		
		//if there are multiple '=' in the extensions, we can only split on first one
		int indexOf = extension.indexOf('=');
		String name = extension.substring(0, indexOf);
		String value = extension.substring(indexOf);
		return new HttpChunkExtension(name, value);
	}

	//Returns true if body read in and false otherwise
	private void readInBody(MementoImpl memento, boolean stripAndCompareLastTwo) {
		HttpPayload message = memento.getHalfParsedMessage();
		DataWrapper dataToRead = memento.getLeftOverData();
		int readableSize = dataToRead.getReadableSize();
		int numBytesNeeded = memento.getNumBytesLeftToRead();
		
		if(numBytesNeeded <= readableSize) {
			List<? extends DataWrapper> split = dataGen.split(dataToRead, numBytesNeeded);
			DataWrapper data = split.get(0);
			if(stripAndCompareLastTwo) {
				List<? extends DataWrapper> splitPieces = dataGen.split(data, data.getReadableSize()-2);
				data = splitPieces.get(0);
				DataWrapper trailer = splitPieces.get(1);
				String trailerStr = trailer.createStringFrom(0, trailer.getReadableSize(), iso8859_1);
				if(!TRAILER_STR.equals(trailerStr))
					throw new IllegalStateException("The chunk did not end with \\r\\n .  The format is invalid");
			}
			
			message.setBody(data);
			memento.setLeftOverData(split.get(1));
			memento.setNumBytesLeftToRead(0);
			memento.getParsedMessages().add(message);
			
			//clear any cached message we were waiting for more data for
			memento.setHalfParsedMessage(null);
			return;
		}
	}

	private HttpMessage parseHttpMessage(DataWrapper toBeParsed, List<Integer> markedPositions) {
		List<String> lines = new ArrayList<>();
		
		//Add the last line..
		markedPositions.add(toBeParsed.getReadableSize());
		int offset = 0;
		for(Integer mark : markedPositions) {
			int len = mark - offset;
			String line = toBeParsed.createStringFrom(offset, len, iso8859_1);
			lines.add(line.trim());
			offset = mark;
		}
		markedPositions.clear();

		//buffer processed...release to be re-used now..
		toBeParsed.releaseUnderlyingBuffers(pool);
		
		String firstLine = lines.get(0).trim();
		
		if(firstLine.startsWith("HTTP/")) {
			return parseResponse(lines);
		} else {
			return parseRequest(lines);
		}
	}

	private HttpMessage parseRequest(List<String> lines) {
		//remove first line...
		String firstLine = lines.remove(0);
		String[] firstLinePieces = firstLine.split("\\s+");
		if(firstLinePieces.length != 3) {
			throw new ParseException("Unable to parse invalid http request due to first line being invalid=" + firstLine+" all Lines="+lines);
		}
		
		HttpRequestMethod method = new HttpRequestMethod(firstLinePieces[0]);
		HttpUri uri = new HttpUri(firstLinePieces[1]);
		
		HttpVersion version = parseVersion(firstLinePieces[2], firstLine);
		
		HttpRequestLine httpRequestLine = new HttpRequestLine();
		httpRequestLine.setMethod(method);
		httpRequestLine.setUri(uri);
		httpRequestLine.setVersion(version);
		
		HttpRequest request = new HttpRequest();
		request.setRequestLine(httpRequestLine);
		
		parseHeaders(lines, request);
		
		return request;
	}

	private HttpVersion parseVersion(String versionString, String firstLine) {
		if(!versionString.startsWith("HTTP/")) {
			throw new ParseException("Invalid version in http request first line not prefixed with HTTP/.  line="+firstLine);
		}
		
		String ver = versionString.substring(5, versionString.length());
		HttpVersion version = new HttpVersion();
		version.setVersion(ver);
		return version;
	}

	private void parseHeaders(List<String> lines, HttpMessage httpMessage) {
		//TODO: one header can be multiline and we need to fix this code for that
		//ie. the spec says you can split a head in multiple lines(ick!!!)
		for(String line : lines) {
			Header header = parseHeader(line);
			httpMessage.addHeader(header);
		}
	}

	private Header parseHeader(String line) {
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

	private HttpMessage parseResponse(List<String> lines) {
		//remove first line...
		String firstLine = lines.remove(0);
		//In the case of response, a reason may contain spaces so we must split on first and second
		//whitespace only
		int indexOf = firstLine.indexOf(" ");
		if(indexOf < 0)
			throw new IllegalArgumentException("The first line of http request is invalid="+ firstLine);
		String versionStr = firstLine.substring(0, indexOf).trim();
		String tail = firstLine.substring(indexOf).trim();
		
		int indexOf2 = tail.indexOf(" ");
		if(indexOf2 < 0)
			throw new IllegalArgumentException("The first line of http request is invalid="+ firstLine);
		String codeStr = tail.substring(0, indexOf2).trim();
		String reason = tail.substring(indexOf2).trim();
		
		HttpVersion version2 = parseVersion(versionStr, firstLine);

		HttpResponseStatus status = new HttpResponseStatus();
		Integer codeVal = toInteger(codeStr, firstLine);
		if(codeVal <= 0 || codeVal >= 1000)
			throw new IllegalArgumentException("invalid status code.  response line="+firstLine);
		status.setCode(codeVal);
		status.setReason(reason);
		
		HttpResponseStatusLine httpRequestLine = new HttpResponseStatusLine();
		httpRequestLine.setStatus(status);
		httpRequestLine.setVersion(version2);
		
		HttpResponse response = new HttpResponse();
		response.setStatusLine(httpRequestLine);

		parseHeaders(lines, response);

		return response;
	}

	@Override
	public HttpPayload unmarshal(byte[] msg) {
		Memento memento = prepareToParse();
		DataWrapper dataWrapper = dataGen.wrapByteArray(msg);
		Memento parsedData = parse(memento, dataWrapper);
		if(parsedData.getStatus() == ParsedStatus.MSG_PARSED_AND_LEFTOVER_DATA)
			throw new IllegalArgumentException("There is more data than one http message.  Use unmarshalAsync instead");
		else if(parsedData.getStatus() == ParsedStatus.NEED_MORE_DATA)
			throw new IllegalArgumentException("This http message is not complete.  Use unmarshalAsynch instead or "
					+ "fix client code to pass in complete http message(or report a bug if it is this libraries fault)");
		
		List<HttpPayload> messages = parsedData.getParsedMessages();
		if(messages.size() != 1)
			throw new IllegalArgumentException("You passed in data for more than one http messages.  number of http messages="+messages.size());
		return messages.get(0);
	}
}
