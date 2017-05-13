package org.webpieces.httpfrontend2.api.mock2;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.webpieces.frontend2.api.FrontendStream;
import org.webpieces.frontend2.api.HttpRequestListener;
import org.webpieces.frontend2.api.Protocol;
import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.RstStreamFrame;

public class MockHttp2RequestListener extends MockSuperclass implements HttpRequestListener {

	private enum Method implements MethodEnum {
		INCOMING, CANCEL
	}
	
	public static class Cancel {
		public FrontendStream stream;
		public RstStreamFrame reset;
		public Cancel(FrontendStream stream, RstStreamFrame reset) {
			super();
			this.stream = stream;
			this.reset = reset;
		}
	}
	
	public static class PassedIn {
		public FrontendStream stream;
		public Http2Headers request;
		public Protocol type;
		public PassedIn(FrontendStream stream, Http2Headers headers, Protocol type) {
			super();
			this.stream = stream;
			this.request = headers;
			this.type = type;
		}
	}
	
	@Override
	public StreamWriter incomingRequest(FrontendStream stream, Http2Headers headers, Protocol type) {
		return (StreamWriter) super.calledMethod(Method.INCOMING, new PassedIn(stream, headers, type));
	}

	public void setDefaultRetVal(StreamWriter writer) {
		super.setDefaultReturnValue(Method.INCOMING, writer);
	}
	
	public int getNumRequestsThatCameIn() {
		return super.getCalledMethodList(Method.INCOMING).size();
	}
	
	public PassedIn getSingleRequest() {
		List<ParametersPassedIn> list = super.getCalledMethodList(Method.INCOMING);
		if(list.size() != 1)
			throw new IllegalArgumentException("method was not called exactly once. numTimes="+list.size());
		return (PassedIn) list.get(0).getArgs()[0];
	}

	@Override
	public void cancelRequest(FrontendStream stream, RstStreamFrame c) {
		super.calledVoidMethod(Method.CANCEL, new Cancel(stream, c));
	}

	@SuppressWarnings("unchecked")
	public List<Cancel> getCancels() {
		Stream<ParametersPassedIn> calledMethodList = super.getCalledMethods(Method.CANCEL);
		Stream<Cancel> retVal = calledMethodList.map(p -> (Cancel)p.getArgs()[0]);

		return retVal.collect(Collectors.toList());	
	}
	
	public Cancel getCancelInfo() {
		List<Cancel> list = getCancels();
		if(list.size() != 1)
			throw new IllegalArgumentException("method was not called exactly once. numTimes="+list.size());
		return (Cancel) list.get(0);
	}

	public int getNumCancelsThatCameIn() {
		return super.getCalledMethodList(Method.CANCEL).size();
	}

	public void addMockStreamToReturn(StreamWriter writer) {
		super.addValueToReturn(Method.INCOMING, writer);
	}

}
