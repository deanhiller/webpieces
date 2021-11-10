package org.webpieces.nio.api.mocks;

import java.nio.ByteBuffer;
import java.util.List;
import org.webpieces.util.futures.XFuture;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;

public class MockDataListener extends MockSuperclass implements DataListener {

	enum Method implements MethodEnum {
		INCOMING_DATA,
		FAR_END_CLOSED,
		FAILURE,
	}

	public MockDataListener() {
		setDefaultReturnValue(Method.INCOMING_DATA, XFuture.completedFuture(null));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public XFuture<Void> incomingData(Channel channel, ByteBuffer b) {
		byte[] newData = new byte[b.remaining()];
		b.get(newData);
		return (XFuture<Void>) super.calledMethod(Method.INCOMING_DATA, channel, newData);
	}

	@Override
	public void farEndClosed(Channel channel) {
		super.calledVoidMethod(Method.FAR_END_CLOSED, channel);
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		byte[] newData = new byte[data.remaining()];
		data.get(newData);
		ByteBuffer clonedData = ByteBuffer.wrap(newData);
		super.calledVoidMethod(Method.FAILURE, channel, clonedData, e);
	}

	public byte[] getSingleData() {
		List<ParametersPassedIn> params = super.getCalledMethodList(Method.INCOMING_DATA);
		if(params.size() != 1)
			throw new IllegalArgumentException("method was called more than once");
		ParametersPassedIn p = params.get(0);
		return (byte[]) p.getArgs()[1];
	}

	public int getNumTimesCalledIncomingData() {
		return super.getCalledMethodList(Method.INCOMING_DATA).size();
	}

	public void addIncomingRetValue(XFuture<Void> future1) {
		super.addValueToReturn(Method.INCOMING_DATA, future1);
	}
}
