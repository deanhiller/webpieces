package com.webpieces.httpparser.impl;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webpieces.httpparser.api.DataWrapper;
import com.webpieces.httpparser.api.DataWrapperGenerator;
import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.HttpParserFactory;
import com.webpieces.httpparser.api.Memento;
import com.webpieces.httpparser.api.ParseException;
import com.webpieces.httpparser.api.ParsedStatus;
import com.webpieces.httpparser.api.common.Header;
import com.webpieces.httpparser.api.dto.HttpMessage;
import com.webpieces.httpparser.api.dto.HttpMessageType;
import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpRequestLine;
import com.webpieces.httpparser.api.dto.HttpRequestMethod;
import com.webpieces.httpparser.api.dto.HttpResponse;
import com.webpieces.httpparser.api.dto.HttpResponseStatus;
import com.webpieces.httpparser.api.dto.HttpResponseStatusLine;
import com.webpieces.httpparser.api.dto.HttpUri;
import com.webpieces.httpparser.api.dto.HttpVersion;
import com.webpieces.httpparser.impl.data.EmptyWrapper;

public class HttpParserImpl implements HttpParser {

	private static final Logger log = LoggerFactory.getLogger(HttpParserImpl.class);
	private static final Charset iso8859_1 = Charset.forName("ISO-8859-1");
	private ConvertAscii conversion = new ConvertAscii();
	private DataWrapperGenerator dataGen = HttpParserFactory.createDataWrapperGenerator();
	
	@Override
	public byte[] marshalToBytes(HttpMessage request) {
		String result = marshalToString(request);
		return result.getBytes(iso8859_1);
	}

	@Override
	public String marshalToString(HttpMessage httpMsg) {
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
		
		if(log.isDebugEnabled()) {
			byte[] someData = moreData.createByteArray();
			String readable = conversion.convertToReadableForm(someData);
			log.info("about to parse=\n\n'"+readable+"'\n\n");
		}

		MementoImpl memento = (MementoImpl) state;
		//initialize state to need more data
		memento.setStatus(ParsedStatus.NEED_MORE_DATA);
		memento.getParsedMessages().clear();
		
		DataWrapper leftOverData = memento.getLeftOverData();
		DataWrapper	allData = dataGen.chainDataWrappers(leftOverData, moreData);
		
		return findCrLnCrLnAndParseMessage(memento, allData);
	}

	private MementoImpl findCrLnCrLnAndParseMessage(
			MementoImpl memento, DataWrapper payload) {
		DataWrapper dataToRead = payload;
		
		//We are looking for the \r\n\r\n  (or \n\n from bad systems) to
		//discover entire payload
		for(int i = 0; i < dataToRead.getReadableSize() - 3; i++) {
			byte firstByte = dataToRead.readByteAt(i);
			byte secondByte = dataToRead.readByteAt(i+1);
			byte thirdByte = dataToRead.readByteAt(i+2);
			byte fourthByte = dataToRead.readByteAt(i+3);
			
			//For debugging to see the 4 bytes that we are processing easier
			byte[] data = dataToRead.createByteArray();
			String fourBytesAre = conversion.convertToReadableForm(data, i, 4);
			
			boolean isFirstCr = conversion.isCarriageReturn(firstByte);
			boolean isSecondLineFeed = conversion.isLineFeed(secondByte);
			boolean isThirdCr = conversion.isCarriageReturn(thirdByte);
			boolean isFourthLineField = conversion.isLineFeed(fourthByte);
			
			if(isFirstCr && isSecondLineFeed && isThirdCr && isFourthLineField) {
				List<Integer> markedPositions = memento.getLeftOverMarkedPositions();
				memento.setLeftOverMarkedPositions(new ArrayList<Integer>());
				List<DataWrapper> tuple = dataGen.split(dataToRead, i+4);
				DataWrapper toBeParsed = tuple.get(0);
				dataToRead = tuple.get(1);
				HttpMessage message = parseHttpMessage(toBeParsed, markedPositions);
				memento.getParsedMessages().add(message);
				//parse message and continue here
				continue;
			}
			
			//mark any positions for \r\n
			if(isFirstCr && isSecondLineFeed) {
				memento.addDemarcation(i);
			}
		}
		
		memento.setLeftOverData(dataToRead);
		if(dataToRead instanceof EmptyWrapper) {
			memento.setStatus(ParsedStatus.ALL_DATA_PARSED);
		}
		return memento;
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

		String firstLine = lines.get(0);
		String[] pieces = firstLine.split("\\s+");
		
		if(pieces[0].startsWith("HTTP/")) {
			return parseResponse(lines, pieces);
		} else {
			return parseRequest(lines, pieces);
		}
	}

	private HttpMessage parseRequest(List<String> lines, String[] firstLinePieces) {
		//remove first line...
		String firstLine = lines.remove(0);
		if(firstLinePieces.length != 3) {
			throw new ParseException("Unable to parse invalid http request due to first line being invalid=" + firstLine);
		}
		
		HttpRequestMethod method = HttpRequestMethod.valueOf(firstLinePieces[0]);
		if(method == null) {
			throw new ParseException("Invalid method in request line of http request.  method="+firstLinePieces[0]);
		}
		
		HttpUri uri = new HttpUri(firstLinePieces[1]);
		
		if(!firstLinePieces[2].startsWith("HTTP/")) {
			throw new ParseException("Invalid version in http request first line not prefixed with HTTP/.  line="+firstLine);
		}
		
		String ver = firstLinePieces[2].substring(5, firstLinePieces[2].length());
		HttpVersion version = new HttpVersion();
		version.setVersion(ver);
		
		HttpRequestLine httpRequestLine = new HttpRequestLine();
		httpRequestLine.setMethod(method);
		httpRequestLine.setUri(uri);
		httpRequestLine.setVersion(version);
		
		HttpRequest request = new HttpRequest();
		request.setRequestLine(httpRequestLine);
		
		//TODO: one header can be multiline and we need to fix this code for that
		for(String line : lines) {
			Header header = parseHeader(line);
			request.addHeader(header);
		}
		
		return request;
	}

	private Header parseHeader(String line) {
		//can't use split in case there are two ':' ...one in the value and one as the delimeter
		int indexOf = line.indexOf(":");
		String value = line.substring(indexOf+1).trim();
		String name = line.substring(0, indexOf);
		Header header = new Header();
		header.setName(name.trim());
		header.setValue(value.trim());
		return header;
	}

	private HttpMessage parseResponse(List<String> lines, String[] pieces) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public HttpMessage unmarshal(byte[] msg) {
		Memento memento = prepareToParse();
		DataWrapper dataWrapper = dataGen.wrapByteArray(msg);
		Memento parsedData = parse(memento, dataWrapper);
		if(parsedData.getStatus() == ParsedStatus.MSG_PARSED_AND_LEFTOVER_DATA)
			throw new IllegalArgumentException("There is more data than one http message.  Use unmarshalAsync instead");
		else if(parsedData.getStatus() == ParsedStatus.NEED_MORE_DATA)
			throw new IllegalArgumentException("This http message is not complete.  Use unmarshalAsynch instead or "
					+ "fix client code to pass in complete http message(or report a bug if it is this libraries fault)");
		
		List<HttpMessage> messages = parsedData.getParsedMessages();
		if(messages.size() != 1)
			throw new IllegalArgumentException("You passed in data for more than one http messages.  number of http messages="+messages.size());
		return messages.get(0);
	}
}
