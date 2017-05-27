package org.webpieces.httpfrontend2.api.mock2;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.webpieces.frontend2.api.FrontendStream;
import org.webpieces.frontend2.api.HttpRequestListener;
import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.api.StreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;

public class MockHttp2RequestListener extends MockSuperclass implements HttpRequestListener {

	private enum Method implements MethodEnum {
		PROCESS, CANCEL, CANCEL_PUSH
	}
	
	public static class Cancel {
		public FrontendStream stream;
		public CancelReason reset;
		public Cancel(FrontendStream stream, CancelReason reset) {
			super();
			this.stream = stream;
			this.reset = reset;
		}
	}
	
	public static class PassedIn {
		public FrontendStream stream;
		public Http2Request request;
		public PassedIn(FrontendStream stream, Http2Request request) {
			super();
			this.stream = stream;
			this.request = request;
		}
	}
	
	public MockHttp2RequestListener() {
		setDefaultReturnValue(Method.CANCEL, CompletableFuture.completedFuture(null));
	}
	
	@Override
	public StreamHandle openStream(FrontendStream stream) {
		return new StreamHandleProxy(stream);
	}

	private class StreamHandleProxy implements StreamHandle {

		private FrontendStream stream;

		public StreamHandleProxy(FrontendStream stream) {
			this.stream = stream;
		}

		@SuppressWarnings("unchecked")
		@Override
		public CompletableFuture<StreamWriter> process(Http2Request request) {
			return (CompletableFuture<StreamWriter>) 
					MockHttp2RequestListener.super.calledMethod(Method.PROCESS, new PassedIn(stream, request));
		}

		@SuppressWarnings("unchecked")
		@Override
		public CompletableFuture<Void> cancel(CancelReason reset) {
			return (CompletableFuture<Void>) 
					MockHttp2RequestListener.super.calledMethod(Method.CANCEL, new Cancel(stream, reset));
		}
		
	}
	
	public void setDefaultRetVal(StreamWriter writer) {
		CompletableFuture<StreamWriter> writerFuture = CompletableFuture.completedFuture(writer);
		super.setDefaultReturnValue(Method.PROCESS, writerFuture);
	}
	
	public void addMockStreamToReturn(StreamWriter writer) {
		CompletableFuture<StreamWriter> writerFuture = CompletableFuture.completedFuture(writer);
		super.addValueToReturn(Method.PROCESS, writerFuture);
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

}
