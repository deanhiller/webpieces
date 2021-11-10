package org.webpieces.httpfrontend2.api.mock2;

import java.util.List;
import org.webpieces.util.futures.XFuture;

import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.HttpStream;
import org.webpieces.frontend2.api.ResponseStream;
import org.webpieces.frontend2.api.StreamListener;
import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

public class MockHttp2RequestListener extends MockSuperclass implements StreamListener {

	private boolean isClosed;
	
	private enum Method implements MethodEnum {
		PROCESS, CANCEL_PUSH
	}
	
	public static class Cancel {
		public ResponseStream stream;
		public CancelReason reset;
		public Cancel(ResponseStream stream, CancelReason reset) {
			super();
			this.stream = stream;
			this.reset = reset;
		}
	}
	
	public static class PassedIn {
		public ResponseStream stream;
		public Http2Request request;
		public PassedIn(ResponseStream stream, Http2Request request) {
			super();
			this.stream = stream;
			this.request = request;
		}
	}
	
	public MockHttp2RequestListener() {
	}
	
	@Override
	public HttpStream openStream(FrontendSocket socket) {
		return new StreamHandleProxy();
	}

	private class StreamHandleProxy implements HttpStream {
		@Override
		public StreamRef incomingRequest(Http2Request request, ResponseStream stream) {
			return (StreamRef) MockHttp2RequestListener.super.calledMethod(Method.PROCESS, new PassedIn(stream, request));
		}
	}
	
	public void setDefaultRetVal(StreamWriter writer) {
		XFuture<StreamWriter> writerFuture = XFuture.completedFuture(writer);
		MockStreamRef ref = new MockStreamRef(writerFuture );		
		super.setDefaultReturnValue(Method.PROCESS, ref);
	}

	public void addMockStreamToReturn(StreamWriter mockSw) {
		XFuture<StreamWriter> future = XFuture.completedFuture(mockSw);
		MockStreamRef ref = new MockStreamRef(future );
		addMockStreamToReturn(ref);
	}
	
	public void addMockStreamToReturn(StreamRef ref) {
		super.addValueToReturn(Method.PROCESS, ref);
	}
	
	public int getNumRequestsThatCameIn() {
		return super.getCalledMethodList(Method.PROCESS).size();
	}
	
	public PassedIn getSingleRequest() {
		List<ParametersPassedIn> list = super.getCalledMethodList(Method.PROCESS);
		if(list.size() != 1)
			throw new IllegalArgumentException("method was not called exactly once. numTimes="+list.size());
		return (PassedIn) list.get(0).getArgs()[0];
	}

	@Override
	public void fireIsClosed(FrontendSocket socketThatClosed) {
		isClosed = true;
	}

	public boolean isClosed() {
		return isClosed;
	}

}
