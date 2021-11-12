package org.webpieces.nio.api.mocks;

import java.nio.ByteBuffer;
import java.util.List;
import org.webpieces.util.futures.XFuture;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;

public class MockSslDataListener extends MockSuperclass implements DataListener {

	enum Method implements MethodEnum {
		INCOMING
	}
	
	private boolean isClosed;

	public MockSslDataListener() {
		setDefaultReturnValue(Method.INCOMING, XFuture.completedFuture(null));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public XFuture<Void> incomingData(Channel channel, ByteBuffer b) {
		return (XFuture<Void>) super.calledMethod(Method.INCOMING, channel, b);
	}

	@Override
	public void farEndClosed(Channel channel) {
		this.isClosed = true;
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
	}

	public synchronized ByteBuffer getSingleBuffer() {
		List<ParametersPassedIn> list = super.getCalledMethodList(Method.INCOMING);
		if(list.size() != 1)
			throw new IllegalStateException("not exactly 1 is called. size="+list.size());
		return (ByteBuffer) list.get(0).getArgs()[1];
	}

	public boolean isClosed() {
		return isClosed;
	}

	public ByteBuffer getFirstBuffer() {
		List<ParametersPassedIn> list = super.getCalledMethodList(Method.INCOMING);
		return (ByteBuffer) list.get(0).getArgs()[1];
	}
	
	public ByteBuffer[] getTwoBuffers() {
		List<ParametersPassedIn> list = super.getCalledMethodList(Method.INCOMING);
		if(list.size() != 2)
			throw new IllegalStateException("not exactly 2 is called. size="+list.size());
		ByteBuffer[] buffers = new ByteBuffer[2];
		buffers[0] = (ByteBuffer) list.get(0).getArgs()[1];
		buffers[1] = (ByteBuffer) list.get(1).getArgs()[1];
		return buffers;
	}

}
