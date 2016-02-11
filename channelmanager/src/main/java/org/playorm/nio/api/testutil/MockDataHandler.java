package org.playorm.nio.api.testutil;

import java.nio.ByteBuffer;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.handlers.DataChunk;
import org.playorm.nio.api.handlers.DataListener;

import biz.xsoftware.impl.mock.MockSuperclass;



/**
 * @author Dean Hiller
 */
public class MockDataHandler extends MockSuperclass implements DataListener {

	public static final String INCOMING_DATA = "incomingData";
	public static final String FAR_END_CLOSED = "farEndClosed";
	private static final String DATA_NOT_RECEIVED = "dataNotReceived";
	
	public MockDataHandler() {
	}
	
	/* (non-Javadoc)
	 * @see api.biz.xsoftware.nio.DataHandler#incomingData(java.nio.ByteBuffer)
	 */
	public void incomingData(Channel channel, DataChunk chunk) {
		ByteBuffer b = chunk.getData();
		Object cloned = CloneByteBuffer.clone(b);
		methodCalled(INCOMING_DATA, new Object[] {channel, cloned});
		chunk.setProcessed("MockDataHandler");
	}

	/* (non-Javadoc)
	 * @see api.biz.xsoftware.nio.DataHandler#farEndClosed()
	 */
	public void farEndClosed(Channel channel) {
		methodCalled(FAR_END_CLOSED, channel);
	}

	public void failure(Channel channel, ByteBuffer data, Exception e) {
		methodCalled(DATA_NOT_RECEIVED, channel);
	}

}
