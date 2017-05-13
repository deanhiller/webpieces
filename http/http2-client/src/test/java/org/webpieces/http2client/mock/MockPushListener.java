package org.webpieces.http2client.mock;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;

import com.webpieces.http2engine.api.client.PushPromiseListener;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class MockPushListener extends MockSuperclass implements PushPromiseListener {

	enum Method implements MethodEnum {
		INCOMING_PUSH,
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<Void> incomingPushPromise(PartialStream response) {
		return (CompletableFuture<Void>) super.calledMethod(Method.INCOMING_PUSH, response);
	}

	public void setIncomingRespDefault(CompletableFuture<Void> retVal) {
		super.setDefaultReturnValue(Method.INCOMING_PUSH, retVal);
	}

	public PartialStream getSingleParam() {
		List<PartialStream> list = getCachedParams();
		if(list.size() != 1)
			throw new IllegalStateException("There is not exactly one return value like expected.  num times method called="+list.size());
		return list.get(0);
	}
	
	public List<PartialStream> getCachedParams() {
		Stream<PartialStream> map = super.getCalledMethods(Method.INCOMING_PUSH).map(s -> (PartialStream)s.getArgs()[0]);
		return map.collect(Collectors.toList());
	}

	public void setDefaultResponse(CompletableFuture<Void> completableFuture) {
		super.setDefaultReturnValue(Method.INCOMING_PUSH, completableFuture);
	}
}
