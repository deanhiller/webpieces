package org.webpieces.nio.api.mocks;

import java.nio.ByteBuffer;
import java.util.List;
import org.webpieces.util.futures.XFuture;

import org.webpieces.asyncserver.api.AsyncDataListener;
import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;

public class MockAsyncListener extends MockSuperclass implements AsyncDataListener {

	enum Method implements MethodEnum {
		INCOMING_DATA,
		FAR_END_CLOSED,
		FAILURE,
		CONNECTION_OPENED
	}
	
	public MockAsyncListener() {
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

	@Override
	public void connectionOpened(TCPChannel proxy, boolean isReadyForWrites) {
		super.calledVoidMethod(Method.CONNECTION_OPENED, new ConnectionOpen(proxy, isReadyForWrites));
	}

	public ConnectionOpen getConnectionOpenedInfo() {
		List<ParametersPassedIn> list = super.getCalledMethodList(Method.CONNECTION_OPENED);
		if(list.size() != 1)
			throw new IllegalStateException("This method was not called exactly once which is expected");
		return (ConnectionOpen) list.get(0).getArgs()[0];
	}
	
	public static class ConnectionOpen {
		public TCPChannel channel;
		public boolean isReadyForWrites;

		public ConnectionOpen(TCPChannel channel, boolean isReadyForWrites) {
			this.channel = channel;
			this.isReadyForWrites = isReadyForWrites;
		}
	}
	
	public int getNumTimesCalledConnectionOpen() {
		return super.getCalledMethodList(Method.CONNECTION_OPENED).size();
	}
	
	public byte[] getSingleData() {
		List<ParametersPassedIn> params = super.getCalledMethodList(Method.INCOMING_DATA);
		if(params.size() != 1)
			throw new IllegalArgumentException("method was not called exactly once");
		ParametersPassedIn p = params.get(0);
		return (byte[]) p.getArgs()[1];
	}

	public int getNumTimesCalledIncomingData() {
		return super.getCalledMethodList(Method.INCOMING_DATA).size();
	}

	public void addIncomingRetValue(XFuture<Void> future1) {
		super.addValueToReturn(Method.INCOMING_DATA, future1);
	}
	
	public Channel getConnectionClosed() {
		List<ParametersPassedIn> list = super.getCalledMethodList(Method.FAR_END_CLOSED);
		if(list.size() != 1)
			throw new IllegalStateException("not exactly 1. size="+list.size());
		return (Channel) list.get(0).getArgs()[0];
	}

	public int getNumConnectionsClosed() {
		List<ParametersPassedIn> list = super.getCalledMethodList(Method.FAR_END_CLOSED);
		return list.size();
	}
}
