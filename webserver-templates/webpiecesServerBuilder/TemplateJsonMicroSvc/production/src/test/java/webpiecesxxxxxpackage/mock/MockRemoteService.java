package webpiecesxxxxxpackage.mock;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.util.futures.XFuture;
import webpiecesxxxxxpackage.deleteme.remoteapi.FetchValueRequest;
import webpiecesxxxxxpackage.deleteme.remoteapi.FetchValueResponse;
import webpiecesxxxxxpackage.deleteme.remoteapi.RemoteApi;

public class MockRemoteService extends MockSuperclass implements RemoteApi {

	public static enum Method implements MethodEnum {
		FETCH_REMOTE_VAL, SEND_DATA
	}

	@Override
	public XFuture<FetchValueResponse> fetchValue(FetchValueRequest request) {
		return (XFuture<FetchValueResponse>) super.calledMethod(Method.FETCH_REMOTE_VAL, request);
	}

	public void addValueToReturn(XFuture<FetchValueResponse> future) {
		super.addValueToReturn(Method.FETCH_REMOTE_VAL, future);
	}

}
