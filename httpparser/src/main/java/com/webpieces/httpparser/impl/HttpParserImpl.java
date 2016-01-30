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
import com.webpieces.httpparser.api.ParsedStatus;
import com.webpieces.httpparser.api.dto.HttpMessage;
import com.webpieces.httpparser.api.dto.HttpMessageType;
import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpRequestLine;
import com.webpieces.httpparser.api.dto.HttpResponse;
import com.webpieces.httpparser.api.dto.HttpResponseStatus;
import com.webpieces.httpparser.api.dto.HttpResponseStatusLine;

public class HttpParserImpl implements HttpParser {

	private static final Logger log = LoggerFactory.getLogger(HttpParserImpl.class);
	private ConvertAscii conversion = new ConvertAscii();
	private DataWrapperGenerator dataGen = HttpParserFactory.createDataWrapperGenerator();
	
	@Override
	public byte[] marshalToBytes(HttpMessage request) {
		String result = marshalToString(request);
		return result.getBytes(Charset.forName("ISO-8859-1"));
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
		return new MementoImpl();
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
		DataWrapper leftOverData = memento.getData();
		DataWrapper allData;
		if(leftOverData == null) {
			allData = moreData;
		} else {
			allData = dataGen.chainDataWrappers(leftOverData, moreData);
		}
		
		boolean hasMore = true;
		while(hasMore) {
			//hasMore = findMessageDemarcation(memento, moreData);
			
		}
		
		return memento;
	}

//	private boolean findMessageDemarcation(
//			MementoImpl memento, byte[] moreData, int offset, int length) {
//		//We are looking for the \r\n\r\n  (or \n\n from bad systems) to
//		//discover entire payload
//		List<Integer> positions = new ArrayList<>();
//		int i = offset;
//		for(; i < length - 3; i++) {
//			byte firstByte = moreData[i];
//			byte secondByte = moreData[i+1];
//			byte thirdByte = moreData[i+2];
//			byte fourthByte = moreData[i+3];
//			
//			//For debugging to see the 4 bytes that we are processing easier
//			//String fourBytesAre = conversion.convertToReadableForm(msg, i, 4);
//			
//			boolean isFirstCr = conversion.isCarriageReturn(firstByte);
//			boolean isSecondLineFeed = conversion.isLineFeed(secondByte);
//			boolean isThirdCr = conversion.isCarriageReturn(thirdByte);
//			boolean isFourthLineField = conversion.isLineFeed(fourthByte);
//			
//			if(isFirstCr && isSecondLineFeed && isThirdCr && isFourthLineField) {
//				
//				CachedData data = new CachedData(moreData, )
//				break;
//			}
//			
//			//mark any positions for \r\n
//			if(isFirstCr && isSecondLineFeed) {
//				positions.add(i);
//			}
//		}
//	}

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
