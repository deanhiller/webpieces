package webpiecesxxxxxpackage.mock;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;

import org.webpieces.mock.ParametersPassedIn;
import webpiecesxxxxxpackage.service.RemoteService;

public class MockRemoteSystem extends MockSuperclass implements RemoteService {

	public static enum Method implements MethodEnum {
		FETCH_REMOTE_VAL, SEND_DATA
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<Integer> fetchRemoteValue(String s, int i) {
		return (CompletableFuture<Integer>) super.calledMethod(Method.FETCH_REMOTE_VAL, s, i);
	}

	@Override
	public void sendData(int num) {
		super.calledVoidMethod(Method.SEND_DATA, num);
	}

	public void addValueToReturn(CompletableFuture<Integer> future) {
		super.addValueToReturn(Method.FETCH_REMOTE_VAL, future);
	}

	public List<Integer> getSendMethodParameters() {
		Stream<ParametersPassedIn> calledMethods2 = super.getCalledMethods(Method.SEND_DATA);
		return calledMethods2
				.map(p -> (Integer)p.getArgs()[0])
				.collect(Collectors.toList());
	}
}
