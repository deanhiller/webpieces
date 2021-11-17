package webpiecesxxxxxpackage.mock;

import java.awt.event.ActionEvent;
import java.util.List;
import org.webpieces.util.futures.XFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;

import org.webpieces.mock.ParametersPassedIn;
import webpiecesxxxxxpackage.service.*;

public class MockRemoteSystem extends MockSuperclass implements RemoteService {

	public static enum Method implements MethodEnum {
		FETCH_REMOTE_VAL, SEND_DATA
	}

	@Override
	public XFuture<FetchValueResponse> fetchValue(FetchValueRequest request) {
		return (XFuture<FetchValueResponse>) super.calledMethod(Method.FETCH_REMOTE_VAL, request);
	}

	@Override
	public XFuture<SendDataResponse> sendData(SendDataRequest num) {
		return (XFuture<SendDataResponse>)super.calledMethod(Method.SEND_DATA, num);
	}

	public void addValueToReturn(XFuture<FetchValueResponse> future) {
		super.addValueToReturn(Method.FETCH_REMOTE_VAL, future);
	}

	public void setSendDefaultRetValue(XFuture<SendDataResponse> future) {
		super.setDefaultReturnValue(Method.SEND_DATA, future);
	}
	public List<SendDataRequest> getSendMethodParameters() {
		Stream<ParametersPassedIn> calledMethods2 = super.getCalledMethods(Method.SEND_DATA);
		return calledMethods2
				.map(p -> (SendDataRequest)p.getArgs()[0])
				.collect(Collectors.toList());
	}
}
