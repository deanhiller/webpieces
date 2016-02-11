package org.playorm.nio.test.fullcontrol;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;

import org.playorm.nio.api.testutil.chanapi.SocketChannel;


public class MySelectableChannel extends SelectableChannel {

	public MySelectableChannel(SocketChannel channel) {
	}
	
	@Override
	public SelectorProvider provider() {
		throw new UnsupportedOperationException("need to add call to channel here");
	}

	@Override
	public int validOps() {
		throw new UnsupportedOperationException("need to add call to channel here");
	}

	@Override
	public boolean isRegistered() {
		throw new UnsupportedOperationException("need to add call to channel here");
	}

	@Override
	public SelectionKey keyFor(Selector sel) {
		throw new UnsupportedOperationException("need to add call to channel here");
	}

	@Override
	public SelectionKey register(Selector sel, int ops, Object att)
			throws ClosedChannelException {
		throw new UnsupportedOperationException("need to add call to channel here");
	}

	@Override
	public SelectableChannel configureBlocking(boolean block)
			throws IOException {
		throw new UnsupportedOperationException("need to add call to channel here");
	}

	@Override
	public boolean isBlocking() {
		throw new UnsupportedOperationException("need to add call to channel here");
	}

	@Override
	public Object blockingLock() {
		throw new UnsupportedOperationException("need to add call to channel here");
	}

	@Override
	protected void implCloseChannel() throws IOException {
		throw new UnsupportedOperationException("need to add call to channel here");
	}

}
