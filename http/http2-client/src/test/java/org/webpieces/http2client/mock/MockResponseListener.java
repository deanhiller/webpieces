package org.webpieces.http2client.mock;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;

import com.webpieces.http2engine.api.client.Http2ResponseListener;
import com.webpieces.http2engine.api.client.PushPromiseListener;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class MockResponseListener extends MockSuperclass implements Http2ResponseListener {

	enum Method implements MethodEnum {
		INCOMING_RESPONSE,
		INCOMING_PUSH,
	}

	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<Void> incomingPartialResponse(PartialStream response) {
		return (CompletableFuture<Void>) super.calledMethod(Method.INCOMING_RESPONSE, response);
	}

	@Override
	public PushPromiseListener newIncomingPush(int streamId) {
		return (PushPromiseListener) super.calledMethod(Method.INCOMING_PUSH, streamId);
	}

	public void addReturnValueIncomingResponse(CompletableFuture<Void> future) {
		super.addValueToReturn(Method.INCOMING_RESPONSE, future);
	}
	public void addReturnValuePush(PushPromiseListener retVal) {
		super.addValueToReturn(Method.INCOMING_PUSH, retVal);
	}

	public Integer getSinglePushStreamId() {
		List<Integer> list = getIncomingPushStreamIds();
		if(list.size() != 1)
			throw new IllegalStateException("There is not exactly one return value like expected.  num times method called="+list.size());
		return list.get(0);
	}
	public List<Integer> getIncomingPushStreamIds() {
		Stream<Integer> map = super.getCalledMethods(Method.INCOMING_PUSH).map(s -> (Integer)s.getArgs()[0]);
		return map.collect(Collectors.toList());
	}
	
	public PartialStream getSingleReturnValueIncomingResponse() {
		List<PartialStream> list = getReturnValuesIncomingResponse();
		if(list.size() != 1)
			throw new IllegalStateException("There is not exactly one return value like expected.  num times method called="+list.size());
		return list.get(0);
	}
	
	public List<PartialStream> getReturnValuesIncomingResponse() {
		Stream<ParametersPassedIn> calledMethodList = super.getCalledMethods(Method.INCOMING_RESPONSE);
		Stream<PartialStream> retVal = calledMethodList.map(p -> (PartialStream)p.getArgs()[0]);

		//clear out read values
		this.calledMethods.remove(Method.INCOMING_RESPONSE);
		
		return retVal.collect(Collectors.toList());
	}

	public void setIncomingRespDefault(CompletableFuture<Void> retVal) {
		super.setDefaultReturnValue(Method.INCOMING_RESPONSE, retVal);
	}

	public void setIncomingPushDefault(PushPromiseListener pushListener) {
		super.setDefaultReturnValue(Method.INCOMING_PUSH, pushListener);
	}

}
