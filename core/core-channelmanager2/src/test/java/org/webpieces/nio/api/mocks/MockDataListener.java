package org.webpieces.nio.api.mocks;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;

public class MockDataListener extends MockSuperclass implements DataListener {

	enum Method implements MethodEnum {
		INCOMING_DATA,
		FAR_END_CLOSED,
		FAILURE,
	}

	public MockDataListener() {
		setDefaultReturnValue(Method.INCOMING_DATA, CompletableFuture.completedFuture(null));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<Void> incomingData(Channel channel, ByteBuffer b) {
		byte[] newData = new byte[b.remaining()];
		b.get(newData);
		ByteBuffer clonedData = ByteBuffer.wrap(newData);
		return (CompletableFuture<Void>) super.calledMethod(Method.INCOMING_DATA, channel, clonedData);
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

}
