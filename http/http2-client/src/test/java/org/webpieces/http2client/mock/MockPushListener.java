package org.webpieces.http2client.mock;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushPromiseListener;
import com.webpieces.http2engine.api.StreamWriter;

public class MockPushListener extends MockSuperclass implements PushPromiseListener {

	enum Method implements MethodEnum {
		INCOMING_PUSH
	}
	
	public MockPushListener() {
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<StreamWriter> incomingPushResponse(Http2Response response) {
		return (CompletableFuture<StreamWriter>) super.calledMethod(Method.INCOMING_PUSH, response);
	}

	public void setIncomingRespDefault(CompletableFuture<Void> retVal) {
		super.setDefaultReturnValue(Method.INCOMING_PUSH, retVal);
	}

	public Http2Response getSingleParam() {
		List<Http2Response> list = getCachedParams();
		if(list.size() != 1)
			throw new IllegalStateException("There is not exactly one return value like expected.  num times method called="+list.size());
		return list.get(0);
	}
	
	public List<Http2Response> getCachedParams() {
		Stream<Http2Response> map = super.getCalledMethods(Method.INCOMING_PUSH).map(s -> (Http2Response)s.getArgs()[0]);
		return map.collect(Collectors.toList());
	}

	public void setDefaultResponse(CompletableFuture<StreamWriter> completableFuture) {
		super.setDefaultReturnValue(Method.INCOMING_PUSH, completableFuture);
	}
	


}
