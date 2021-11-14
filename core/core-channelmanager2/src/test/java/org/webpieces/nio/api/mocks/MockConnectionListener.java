package org.webpieces.nio.api.mocks;

import java.util.ArrayList;
import java.util.List;
import org.webpieces.util.futures.XFuture;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;

public class MockConnectionListener implements ConnectionListener {

	private List<Channel> channels = new ArrayList<>();
	private DataListener dataListener;

	public MockConnectionListener(MockMulithreadedSslDataListener serverListener) {
		this.dataListener = serverListener;
	}

	@Override
	public XFuture<DataListener> connected(Channel channel, boolean isReadyForWrites) {
		channels.add(channel);
		return XFuture.completedFuture(dataListener);
	}

	@Override
	public void failed(RegisterableChannel channel, Throwable e) {
	}

}
