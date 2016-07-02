package org.webpieces.nio.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;

public class MockConnectionListener implements ConnectionListener {

	private List<Channel> channels = new ArrayList<>();
	private DataListener dataListener;

	public MockConnectionListener(MockDataListener serverListener) {
		this.dataListener = serverListener;
	}

	@Override
	public CompletableFuture<DataListener> connected(Channel channel, boolean s) {
		channels.add(channel);
		return CompletableFuture.completedFuture(dataListener);
	}

	@Override
	public void failed(RegisterableChannel channel, Throwable e) {
	}

}
