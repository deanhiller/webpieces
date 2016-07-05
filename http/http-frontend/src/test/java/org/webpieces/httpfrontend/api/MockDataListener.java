package org.webpieces.httpfrontend.api;

import java.nio.ByteBuffer;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;

public class MockDataListener implements DataListener {

	private boolean isClosed;

	public boolean isClosed() {
		return isClosed;
	}

	@Override
	public void incomingData(Channel channel, ByteBuffer b) {
	}

	@Override
	public void farEndClosed(Channel channel) {
		isClosed = true;
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void applyBackPressure(Channel channel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void releaseBackPressure(Channel channel) {
		// TODO Auto-generated method stub
		
	}

}
