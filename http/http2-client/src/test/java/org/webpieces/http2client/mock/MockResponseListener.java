package org.webpieces.http2client.mock;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;

import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushPromiseListener;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.ResponseHandler;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;

public class MockResponseListener extends MockSuperclass implements ResponseHandler {

	enum Method implements MethodEnum {
		INCOMING_RESPONSE,
		CANCEL,
		INCOMING_PUSH,
		CANCEL_PUSH
	}

	public MockResponseListener() {
		setDefaultReturnValue(Method.CANCEL, CompletableFuture.completedFuture(null));
		setDefaultReturnValue(Method.CANCEL_PUSH, CompletableFuture.completedFuture(null));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<StreamWriter> process(Http2Response response) {
		return (CompletableFuture<StreamWriter>) super.calledMethod(Method.INCOMING_RESPONSE, response);
	}
	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<Void> cancel(CancelReason frame) {
		return (CompletableFuture<Void>) super.calledMethod(Method.CANCEL, frame);
	}
	@Override
	public PushStreamHandle openPushStream() {
		return new MockPushStreamHandle();
	}

	public void addReturnValueIncomingResponse(CompletableFuture<StreamWriter> future) {
		super.addValueToReturn(Method.INCOMING_RESPONSE, future);
	}
	
	public Http2Response getSingleReturnValueIncomingResponse() {
		List<Http2Response> list = getReturnValuesIncomingResponse();
		if(list.size() != 1)
			throw new IllegalStateException("There is not exactly one return value like expected.  num times method called="+list.size());
		return list.get(0);
	}
	
	public List<Http2Response> getReturnValuesIncomingResponse() {
		Stream<ParametersPassedIn> calledMethodList = super.getCalledMethods(Method.INCOMING_RESPONSE);
		Stream<Http2Response> retVal = calledMethodList.map(p -> (Http2Response)p.getArgs()[0]);
		return retVal.collect(Collectors.toList());
	}

	public void setIncomingRespDefault(CompletableFuture<StreamWriter> retVal) {
		super.setDefaultReturnValue(Method.INCOMING_RESPONSE, retVal);
	}

	public List<CancelReason> getRstStreams() {
		Stream<ParametersPassedIn> calledMethodList = super.getCalledMethods(Method.CANCEL);
		Stream<CancelReason> retVal = calledMethodList.map(p -> (CancelReason)p.getArgs()[0]);
		return retVal.collect(Collectors.toList());
	}

	public CancelReason getSingleRstStream() {
		List<CancelReason> rstStreams = getRstStreams();
		if(rstStreams.size() != 1)
			throw new IllegalStateException("There is not exactly one return value like expected.  num times method called="+rstStreams.size());
		return rstStreams.get(0);
	}

	
	private class MockPushStreamHandle implements PushStreamHandle {

		@SuppressWarnings("unchecked")
		@Override
		public CompletableFuture<PushPromiseListener> process(Http2Push headers) {
			return (CompletableFuture<PushPromiseListener>) 
					MockResponseListener.super.calledMethod(Method.INCOMING_PUSH, headers);
		}

		@SuppressWarnings("unchecked")
		@Override
		public CompletableFuture<Void> cancelPush(CancelReason reset) {
			return (CompletableFuture<Void>) 
					MockResponseListener.super.calledMethod(Method.CANCEL_PUSH, reset);
		}

	}
	
	public void addReturnValuePush(PushPromiseListener retVal) {
		super.addValueToReturn(Method.INCOMING_PUSH, CompletableFuture.completedFuture(retVal));
	}
	
	public void setIncomingPushDefault(PushPromiseListener pushListener) {
		super.setDefaultReturnValue(Method.INCOMING_PUSH, CompletableFuture.completedFuture(pushListener));
	}
	
	public Http2Push getSinglePush() {
		List<Http2Push> list = getIncomingPushes();
		if(list.size() != 1)
			throw new IllegalStateException("There is not exactly one return value like expected.  num times method called="+list.size());
		return list.get(0);
	}
	public List<Http2Push> getIncomingPushes() {
		Stream<Http2Push> map = super.getCalledMethods(Method.INCOMING_PUSH).map(s -> (Http2Push)s.getArgs()[0]);
		return map.collect(Collectors.toList());
	}

	public CancelReason getSingleCancelPush() {
		List<CancelReason> list = getPushCancels();
		if(list.size() != 1)
			throw new IllegalStateException("There is not exactly one return value like expected.  num times method called="+list.size());
		return list.get(0);
	}
	public List<CancelReason> getPushCancels() {
		Stream<CancelReason> map = super.getCalledMethods(Method.CANCEL_PUSH).map(s -> (CancelReason)s.getArgs()[0]);
		return map.collect(Collectors.toList());
	}
}
