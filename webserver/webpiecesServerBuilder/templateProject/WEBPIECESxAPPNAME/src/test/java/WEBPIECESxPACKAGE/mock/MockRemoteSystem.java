package WEBPIECESxPACKAGE.mock;

import java.util.concurrent.CompletableFuture;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;

import WEBPIECESxPACKAGE.base.libs.RemoteService;

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
		super.calledMethod(Method.SEND_DATA, num);
	}

	public void addValueToReturn(CompletableFuture<Integer> future) {
		super.addValueToReturn(Method.FETCH_REMOTE_VAL, future);
	}
	
}
