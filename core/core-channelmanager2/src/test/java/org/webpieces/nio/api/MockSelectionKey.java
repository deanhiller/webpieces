package org.webpieces.nio.api;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class MockSelectionKey extends SelectionKey {

	private int readyOps;
	private int interestOps;

	public MockSelectionKey() {
	}

	@Override
	public SelectableChannel channel() {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public Selector selector() {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public void cancel() {
	}

	@Override
	public int interestOps() {
		return interestOps;
	}

	@Override
	public SelectionKey interestOps(int ops) {
		interestOps = ops;
		return this;
	}

	@Override
	public int readyOps() {
		return readyOps;
	}

	public void setConnectReady() {
		readyOps = readyOps | OP_CONNECT;
	}

	public void setWriteReady() {
		readyOps = readyOps | OP_WRITE;
	}

}
